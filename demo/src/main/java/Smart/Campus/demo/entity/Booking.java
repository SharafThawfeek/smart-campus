package Smart.Campus.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Booking entity representing resource reservation requests.
 * Workflow: PENDING → APPROVED/REJECTED → CANCELLED.
 * Enforces no overlapping bookings for the same resource.
 */
@Entity
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_id")
    private Long bookingId;

    /** The user who made the booking request */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** The resource being booked */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_id", nullable = false)
    private Resource resource;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(columnDefinition = "TEXT")
    private String purpose;

    @Column(length = 50)
    @Builder.Default
    private String status = "PENDING"; // PENDING, APPROVED, REJECTED, CANCELLED

    /** Reason provided by admin for approval/rejection */
    @Column(name = "admin_reason", columnDefinition = "TEXT")
    private String adminReason;
}
