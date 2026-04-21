package Smart.Campus.demo.controller;

import Smart.Campus.demo.dto.CommentDto;
import Smart.Campus.demo.dto.TicketDto;
import Smart.Campus.demo.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * REST Controller for Maintenance & Incident Ticketing (Module C).
 * Handles ticket CRUD, file attachments, comments, and status workflow.
 * 
 * Base path: /api/v1/tickets
 * 
 * Workflow: OPEN → IN_PROGRESS → RESOLVED → CLOSED (or REJECTED)
 */
@RestController
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    /**
     * Get all tickets with optional filters.
     * Regular users see only their own; admins/technicians see all.
     * 
     * GET /api/v1/tickets?status=OPEN&priority=HIGH&category=Electrical
     * Status: 200 OK
     */
    @GetMapping
    public ResponseEntity<List<TicketDto.Response>> getTickets(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String category,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        boolean isAdmin = isAdminOrTechnician(authentication);

        List<TicketDto.Response> tickets = ticketService.getTickets(userId, status, priority, category, isAdmin);
        return ResponseEntity.ok(tickets);
    }

    /**
     * Get a single ticket by ID with all attachments and comments.
     * 
     * GET /api/v1/tickets/{id}
     * Status: 200 OK | 404 Not Found
     */
    @GetMapping("/{id}")
    public ResponseEntity<TicketDto.Response> getTicketById(@PathVariable Long id) {
        TicketDto.Response ticket = ticketService.getTicketById(id);
        return ResponseEntity.ok(ticket);
    }

    /**
     * Create a new incident ticket with optional image attachments (up to 3).
     * Uses multipart/form-data to support file upload.
     * 
     * POST /api/v1/tickets
     * Status: 201 Created | 400 Bad Request
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TicketDto.Response> createTicket(
            @RequestParam(required = false) Long resourceId,
            @RequestParam String category,
            @RequestParam String description,
            @RequestParam String priority,
            @RequestParam(required = false) List<MultipartFile> files,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();

        TicketDto.CreateRequest request = TicketDto.CreateRequest.builder()
                .resourceId(resourceId)
                .category(category)
                .description(description)
                .priority(priority)
                .build();

        TicketDto.Response created = ticketService.createTicket(userId, request, files);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    /**
     * Update ticket status and/or assign technician (ADMIN/TECHNICIAN only).
     * 
     * PUT /api/v1/tickets/{id}
     * Status: 200 OK | 404 Not Found | 400 Bad Request
     */
    @PutMapping("/{id}")
    public ResponseEntity<TicketDto.Response> updateTicket(
            @PathVariable Long id,
            @RequestBody TicketDto.UpdateRequest request) {
        TicketDto.Response updated = ticketService.updateTicket(id, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * Delete a ticket.
     * 
     * DELETE /api/v1/tickets/{id}
     * Status: 204 No Content | 404 Not Found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTicket(@PathVariable Long id) {
        ticketService.deleteTicket(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Add a comment to a ticket.
     * 
     * POST /api/v1/tickets/{id}/comments
     * Status: 201 Created | 404 Not Found
     */
    @PostMapping("/{id}/comments")
    public ResponseEntity<CommentDto.Response> addComment(
            @PathVariable Long id,
            @Valid @RequestBody CommentDto.Request request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        CommentDto.Response comment = ticketService.addComment(id, userId, request);
        return new ResponseEntity<>(comment, HttpStatus.CREATED);
    }

    /**
     * Update a comment (owner only).
     * 
     * PUT /api/v1/tickets/{ticketId}/comments/{commentId}
     * Status: 200 OK | 404 Not Found | 403 Forbidden
     */
    @PutMapping("/{ticketId}/comments/{commentId}")
    public ResponseEntity<CommentDto.Response> updateComment(
            @PathVariable Long ticketId,
            @PathVariable Long commentId,
            @Valid @RequestBody CommentDto.Request request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        CommentDto.Response updated = ticketService.updateComment(ticketId, commentId, userId, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * Delete a comment (owner or admin only).
     * 
     * DELETE /api/v1/tickets/{ticketId}/comments/{commentId}
     * Status: 204 No Content | 404 Not Found | 403 Forbidden
     */
    @DeleteMapping("/{ticketId}/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long ticketId,
            @PathVariable Long commentId,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        boolean isAdmin = isAdmin(authentication);
        ticketService.deleteComment(ticketId, commentId, userId, isAdmin);
        return ResponseEntity.noContent().build();
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_ADMIN"));
    }

    private boolean isAdminOrTechnician(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_ADMIN") || a.equals("ROLE_TECHNICIAN"));
    }
}
