package Smart.Campus.demo.controller;

import Smart.Campus.demo.dto.NotificationDto;
import Smart.Campus.demo.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Notifications (Module D).
 * Handles notification retrieval, read status, and deletion.
 * 
 * Base path: /api/v1/notifications
 */
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
/**
 * @author mohsh
 */
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * Get all notifications for the current user (newest first).
     * 
     * GET /api/v1/notifications
     * Status: 200 OK
     */
    @GetMapping
    public ResponseEntity<List<NotificationDto.Response>> getNotifications(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        List<NotificationDto.Response> notifications = notificationService.getUserNotifications(userId);
        return ResponseEntity.ok(notifications);
    }

    /**
     * Get unread notification count for the current user.
     * 
     * GET /api/v1/notifications/unread-count
     * Status: 200 OK
     */
    @GetMapping("/unread-count")
    public ResponseEntity<NotificationDto.UnreadCount> getUnreadCount(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        long count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(new NotificationDto.UnreadCount(count));
    }

    /**
     * Mark a single notification as read.
     * 
     * PUT /api/v1/notifications/{id}/read
     * Status: 200 OK | 404 Not Found
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Mark all notifications as read for the current user.
     * 
     * PUT /api/v1/notifications/read-all
     * Status: 200 OK
     */
    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }

    /**
     * Delete a notification.
     * 
     * DELETE /api/v1/notifications/{id}
     * Status: 204 No Content | 404 Not Found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.noContent().build();
    }
}
