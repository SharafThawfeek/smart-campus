package Smart.Campus.demo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

/**
 * DTOs for Booking-related operations (Module B).
 */
public class BookingDto {

    /** Request DTO for creating a booking */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateRequest {
        @NotNull(message = "Resource ID is required")
        private Long resourceId;

        @NotNull(message = "Start time is required")
        private LocalDateTime startTime;

        @NotNull(message = "End time is required")
        private LocalDateTime endTime;

        private String purpose;
    }

    /** Request DTO for admin review (approve/reject) */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReviewRequest {
        private String adminReason;
    }

    /** Response DTO - what the API returns */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long bookingId;
        private UserDto.Response user;
        private ResourceDto.Response resource;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private String purpose;
        private String status;
        private String adminReason;
    }
}
