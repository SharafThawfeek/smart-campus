package Smart.Campus.demo.repository;

import Smart.Campus.demo.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for Booking entity.
 * Supports conflict detection and filtered queries.
 */
@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    /** Find all bookings by a specific user */
    List<Booking> findByUserUserId(Long userId);

    /** Find all bookings for a specific resource */
    List<Booking> findByResourceResourceId(Long resourceId);

    /** Find bookings by status */
    List<Booking> findByStatus(String status);

    /**
     * Check for scheduling conflicts: find APPROVED bookings that overlap
     * with the requested time range for the same resource.
     * Two bookings overlap if: existingStart < newEnd AND existingEnd > newStart
     */
    @Query("SELECT b FROM Booking b WHERE b.resource.resourceId = :resourceId " +
           "AND b.status = 'APPROVED' " +
           "AND b.startTime < :endTime " +
           "AND b.endTime > :startTime")
    List<Booking> findConflictingBookings(
            @Param("resourceId") Long resourceId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /** Filter bookings by multiple criteria */
    @Query("SELECT b FROM Booking b WHERE " +
           "(:userId IS NULL OR b.user.userId = :userId) AND " +
           "(:resourceId IS NULL OR b.resource.resourceId = :resourceId) AND " +
           "(:status IS NULL OR b.status = :status)")
    List<Booking> findByFilters(
            @Param("userId") Long userId,
            @Param("resourceId") Long resourceId,
            @Param("status") String status
    );
}
