package Smart.Campus.demo.service;

import Smart.Campus.demo.dto.CommentDto;
import Smart.Campus.demo.dto.TicketDto;
import Smart.Campus.demo.entity.*;
import Smart.Campus.demo.exception.ResourceNotFoundException;
import Smart.Campus.demo.exception.UnauthorizedException;
import Smart.Campus.demo.mapper.EntityMapper;
import Smart.Campus.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service layer for Incident Ticketing (Module C).
 * Handles ticket CRUD, file attachments (up to 3 images), comments, and technician assignment.
 * Workflow: OPEN → IN_PROGRESS → RESOLVED → CLOSED (or REJECTED).
 */
@Service
@RequiredArgsConstructor
@Transactional
public class TicketService {

    private final IncidentTicketRepository ticketRepository;
    private final TicketAttachmentRepository attachmentRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final ResourceRepository resourceRepository;
    private final NotificationService notificationService;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    /**
     * Get all tickets with optional filters.
     * Regular users see only their own; admins/technicians see all.
     */
    @Transactional(readOnly = true)
    public List<TicketDto.Response> getTickets(Long userId, String status, String priority, String category, boolean isAdmin) {
        List<IncidentTicket> tickets;

        if (isAdmin) {
            tickets = ticketRepository.findByFilters(null, status, priority, category);
        } else {
            tickets = ticketRepository.findByFilters(userId, status, priority, category);
        }

        return tickets.stream()
                .map(EntityMapper::toTicketResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get a single ticket by ID with all attachments and comments.
     */
    @Transactional(readOnly = true)
    public TicketDto.Response getTicketById(Long ticketId) {
        IncidentTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with ID: " + ticketId));
        return EntityMapper.toTicketResponse(ticket);
    }

    /**
     * Create a new incident ticket with optional image attachments (up to 3).
     */
    public TicketDto.Response createTicket(Long userId, TicketDto.CreateRequest request, List<MultipartFile> files) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        Resource resource = null;
        if (request.getResourceId() != null) {
            resource = resourceRepository.findById(request.getResourceId())
                    .orElseThrow(() -> new ResourceNotFoundException("Resource not found with ID: " + request.getResourceId()));
        }

        // Validate priority
        String priority = request.getPriority().toUpperCase();
        if (!List.of("LOW", "MEDIUM", "HIGH").contains(priority)) {
            throw new IllegalArgumentException("Priority must be LOW, MEDIUM, or HIGH");
        }

        IncidentTicket ticket = IncidentTicket.builder()
                .user(user)
                .resource(resource)
                .category(request.getCategory())
                .description(request.getDescription())
                .priority(priority)
                .status("OPEN")
                .build();

        IncidentTicket saved = ticketRepository.save(ticket);

        // Handle file attachments (up to 3)
        if (files != null && !files.isEmpty()) {
            if (files.size() > 3) {
                throw new IllegalArgumentException("Maximum 3 attachments allowed per ticket");
            }
            for (MultipartFile file : files) {
                String fileUrl = saveFile(file);
                TicketAttachment attachment = TicketAttachment.builder()
                        .ticket(saved)
                        .fileUrl(fileUrl)
                        .build();
                attachmentRepository.save(attachment);
            }
        }

        // Re-fetch to include attachments
        return EntityMapper.toTicketResponse(ticketRepository.findById(saved.getTicketId()).orElse(saved));
    }

    /**
     * Update ticket status and/or assign a technician (ADMIN/TECHNICIAN).
     */
    public TicketDto.Response updateTicket(Long ticketId, TicketDto.UpdateRequest request) {
        IncidentTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with ID: " + ticketId));

        if (request.getStatus() != null) {
            String newStatus = request.getStatus().toUpperCase();
            validateStatusTransition(ticket.getStatus(), newStatus);
            ticket.setStatus(newStatus);

            // Notify ticket owner of status change
            notificationService.createNotification(ticket.getUser(),
                    "Your ticket #" + ticketId + " status changed to " + newStatus);
        }

        if (request.getAssignedTechnicianId() != null) {
            User technician = userRepository.findById(request.getAssignedTechnicianId())
                    .orElseThrow(() -> new ResourceNotFoundException("Technician not found with ID: " + request.getAssignedTechnicianId()));
            ticket.setAssignedTechnician(technician);

            // Notify the technician
            notificationService.createNotification(technician,
                    "You have been assigned to ticket #" + ticketId + ": " + ticket.getCategory());
        }

        IncidentTicket updated = ticketRepository.save(ticket);
        return EntityMapper.toTicketResponse(updated);
    }

    /**
     * Delete a ticket.
     */
    public void deleteTicket(Long ticketId) {
        if (!ticketRepository.existsById(ticketId)) {
            throw new ResourceNotFoundException("Ticket not found with ID: " + ticketId);
        }
        ticketRepository.deleteById(ticketId);
    }

    /**
     * Add a comment to a ticket.
     */
    public CommentDto.Response addComment(Long ticketId, Long userId, CommentDto.Request request) {
        IncidentTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with ID: " + ticketId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        Comment comment = Comment.builder()
                .ticket(ticket)
                .user(user)
                .content(request.getContent())
                .build();

        Comment saved = commentRepository.save(comment);

        // Notify ticket owner about new comment (if commenter is not the owner)
        if (!ticket.getUser().getUserId().equals(userId)) {
            notificationService.createNotification(ticket.getUser(),
                    user.getName() + " commented on your ticket #" + ticketId);
        }

        return EntityMapper.toCommentResponse(saved);
    }

    /**
     * Update a comment (owner only).
     */
    public CommentDto.Response updateComment(Long ticketId, Long commentId, Long userId, CommentDto.Request request) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with ID: " + commentId));

        // Verify ticket association
        if (!comment.getTicket().getTicketId().equals(ticketId)) {
            throw new IllegalArgumentException("Comment does not belong to the specified ticket");
        }

        // Verify ownership
        if (!comment.getUser().getUserId().equals(userId)) {
            throw new UnauthorizedException("You can only edit your own comments");
        }

        comment.setContent(request.getContent());
        Comment updated = commentRepository.save(comment);
        return EntityMapper.toCommentResponse(updated);
    }

    /**
     * Delete a comment (owner or admin only).
     */
    public void deleteComment(Long ticketId, Long commentId, Long userId, boolean isAdmin) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with ID: " + commentId));

        if (!comment.getTicket().getTicketId().equals(ticketId)) {
            throw new IllegalArgumentException("Comment does not belong to the specified ticket");
        }

        if (!isAdmin && !comment.getUser().getUserId().equals(userId)) {
            throw new UnauthorizedException("You can only delete your own comments");
        }

        commentRepository.deleteById(commentId);
    }

    /**
     * Validate ticket status transitions.
     * Allowed: OPEN → IN_PROGRESS → RESOLVED → CLOSED, or any → REJECTED
     */
    private void validateStatusTransition(String currentStatus, String newStatus) {
        if ("REJECTED".equals(newStatus)) {
            return; // Admin can reject from any status
        }

        boolean valid = switch (currentStatus) {
            case "OPEN" -> "IN_PROGRESS".equals(newStatus);
            case "IN_PROGRESS" -> "RESOLVED".equals(newStatus);
            case "RESOLVED" -> "CLOSED".equals(newStatus);
            default -> false;
        };

        if (!valid) {
            throw new IllegalArgumentException(
                    "Invalid status transition from " + currentStatus + " to " + newStatus);
        }
    }

    /**
     * Save an uploaded file to the file system and return the URL path.
     */
    private String saveFile(MultipartFile file) {
        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : "";
            String filename = UUID.randomUUID().toString() + extension;

            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return "/uploads/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("Failed to save file: " + e.getMessage(), e);
        }
    }
}
