package Smart.Campus.demo.mapper;

import Smart.Campus.demo.dto.*;
import Smart.Campus.demo.entity.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for mapping between entities and DTOs.
 * Ensures entities are never directly exposed to the API layer.
 */
public class EntityMapper {

    private EntityMapper() {
        // Utility class - prevent instantiation
    }

    // ========== User Mappings ==========

    public static UserDto.Response toUserResponse(User user) {
        if (user == null) return null;
        return UserDto.Response.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole())
                .profilePicture(user.getProfilePicture())
                .build();
    }

    // ========== Resource Mappings ==========

    public static ResourceDto.Response toResourceResponse(Resource resource) {
        if (resource == null) return null;
        return ResourceDto.Response.builder()
                .resourceId(resource.getResourceId())
                .name(resource.getName())
                .type(resource.getType())
                .capacity(resource.getCapacity())
                .location(resource.getLocation())
                .status(resource.getStatus())
                .build();
    }

    public static Resource toResourceEntity(ResourceDto.Request request) {
        return Resource.builder()
                .name(request.getName())
                .type(request.getType().toUpperCase())
                .capacity(request.getCapacity())
                .location(request.getLocation())
                .status(request.getStatus() != null ? request.getStatus().toUpperCase() : "ACTIVE")
                .build();
    }

    // ========== Booking Mappings ==========

    public static BookingDto.Response toBookingResponse(Booking booking) {
        if (booking == null) return null;
        return BookingDto.Response.builder()
                .bookingId(booking.getBookingId())
                .user(toUserResponse(booking.getUser()))
                .resource(toResourceResponse(booking.getResource()))
                .startTime(booking.getStartTime())
                .endTime(booking.getEndTime())
                .purpose(booking.getPurpose())
                .status(booking.getStatus())
                .adminReason(booking.getAdminReason())
                .build();
    }

    // ========== Ticket Mappings ==========

    public static TicketDto.Response toTicketResponse(IncidentTicket ticket) {
        if (ticket == null) return null;
        return TicketDto.Response.builder()
                .ticketId(ticket.getTicketId())
                .user(toUserResponse(ticket.getUser()))
                .resource(toResourceResponse(ticket.getResource()))
                .category(ticket.getCategory())
                .description(ticket.getDescription())
                .priority(ticket.getPriority())
                .status(ticket.getStatus())
                .assignedTechnician(toUserResponse(ticket.getAssignedTechnician()))
                .attachments(ticket.getAttachments() != null
                        ? ticket.getAttachments().stream()
                            .map(EntityMapper::toAttachmentResponse)
                            .collect(Collectors.toList())
                        : List.of())
                .comments(ticket.getComments() != null
                        ? ticket.getComments().stream()
                            .map(EntityMapper::toCommentResponse)
                            .collect(Collectors.toList())
                        : List.of())
                .build();
    }

    public static TicketDto.AttachmentResponse toAttachmentResponse(TicketAttachment attachment) {
        if (attachment == null) return null;
        return TicketDto.AttachmentResponse.builder()
                .attachmentId(attachment.getAttachmentId())
                .fileUrl(attachment.getFileUrl())
                .build();
    }

    // ========== Comment Mappings ==========

    public static CommentDto.Response toCommentResponse(Comment comment) {
        if (comment == null) return null;
        return CommentDto.Response.builder()
                .commentId(comment.getCommentId())
                .user(toUserResponse(comment.getUser()))
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .build();
    }

    // ========== Notification Mappings ==========

    public static NotificationDto.Response toNotificationResponse(Notification notification) {
        if (notification == null) return null;
        return NotificationDto.Response.builder()
                .notificationId(notification.getNotificationId())
                .message(notification.getMessage())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
