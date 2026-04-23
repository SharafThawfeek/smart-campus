package Smart.Campus.demo.service;

import Smart.Campus.demo.entity.Notification;
import Smart.Campus.demo.entity.User;
import Smart.Campus.demo.dto.NotificationDto;
import Smart.Campus.demo.exception.ResourceNotFoundException;
import Smart.Campus.demo.mapper.EntityMapper;
import Smart.Campus.demo.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for Notifications (Module D).
 * Creates and manages user notifications triggered by system events.
 */
@Service
@RequiredArgsConstructor
@Transactional
/**
 * @author mohsh
 */
public class NotificationService {

    private final NotificationRepository notificationRepository;

    /**
     * Create a notification for a user (called internally by other services).
     */
    public void createNotification(User user, String message) {
        Notification notification = Notification.builder()
                .user(user)
                .message(message)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
        notificationRepository.save(notification);
    }

    /**
     * Get all notifications for a user (newest first).
     */
    @Transactional(readOnly = true)
    public List<NotificationDto.Response> getUserNotifications(Long userId) {
        return notificationRepository.findByUserUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(EntityMapper::toNotificationResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get unread notification count for a user.
     */
    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserUserIdAndIsReadFalse(userId);
    }

    /**
     * Mark a single notification as read.
     */
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with ID: " + notificationId));
        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    /**
     * Mark all notifications as read for a user.
     */
    public void markAllAsRead(Long userId) {
        List<Notification> unread = notificationRepository.findByUserUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
        unread.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(unread);
    }

    /**
     * Delete a notification.
     */
    public void deleteNotification(Long notificationId) {
        if (!notificationRepository.existsById(notificationId)) {
            throw new ResourceNotFoundException("Notification not found with ID: " + notificationId);
        }
        notificationRepository.deleteById(notificationId);
    }
}
