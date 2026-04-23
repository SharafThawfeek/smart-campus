package Smart.Campus.demo.dto;

import lombok.*;

import java.time.LocalDateTime;

/**
 * DTOs for Notification-related operations (Module D).
 */
public class NotificationDto {

    /** Response DTO */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long notificationId;
        private String message;
        private Boolean isRead;
        private LocalDateTime createdAt;
    }

    /** Unread count response */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UnreadCount {
        private long count;
    }
}
