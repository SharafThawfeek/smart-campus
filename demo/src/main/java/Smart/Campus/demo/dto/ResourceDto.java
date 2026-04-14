package Smart.Campus.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.*;

/**
 * DTOs for Resource-related operations (Module A).
 */
public class ResourceDto {

    /** Request DTO for creating/updating a resource */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        @NotBlank(message = "Resource name is required")
        private String name;

        @NotBlank(message = "Resource type is required (ROOM, LAB, EQUIPMENT)")
        private String type;

        @Positive(message = "Capacity must be a positive number")
        private Integer capacity;

        private String location;

        private String status;
    }

    /** Response DTO - what the API returns */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long resourceId;
        private String name;
        private String type;
        private Integer capacity;
        private String location;
        private String status;
    }
}
