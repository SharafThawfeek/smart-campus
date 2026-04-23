package Smart.Campus.demo.repository;

import Smart.Campus.demo.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Notification entity.
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /** Find all notifications for a user, newest first */
    List<Notification> findByUserUserIdOrderByCreatedAtDesc(Long userId);

    /** Find unread notifications for a user */
    List<Notification> findByUserUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);

    /** Count unread notifications for a user */
    long countByUserUserIdAndIsReadFalse(Long userId);
}
