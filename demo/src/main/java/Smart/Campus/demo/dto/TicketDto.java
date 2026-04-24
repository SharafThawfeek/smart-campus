package Smart.Campus.demo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.List;

/**
 * DTOs for IncidentTicket-related operations (Module C).
 */
public class TicketDto {

    /** Request DTO for creating a ticket */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateRequest {
        private Long resourceId; // optional

        @NotBlank(message = "Category is required")
        private String category;

        @NotBlank(message = "Description is required")
        private String description;

        @NotBlank(message = "Priority is required (LOW, MEDIUM, HIGH)")
        private String priority;
    }

    /** Request DTO for updating ticket status / assigning technician */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        private String status;
        private Long assignedTechnicianId;
    }

    /** Response DTO for a ticket attachment */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AttachmentResponse {
        private Long attachmentId;
        private String fileUrl;
    }

    /** Response DTO - what the API returns */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long ticketId;
        private UserDto.Response user;
        private ResourceDto.Response resource;
        private String category;
        private String description;
        private String priority;
        private String status;
        private UserDto.Response assignedTechnician;
        private List<AttachmentResponse> attachments;
        private List<CommentDto.Response> comments;
    }
}
