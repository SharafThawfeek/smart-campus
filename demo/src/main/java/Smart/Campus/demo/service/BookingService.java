package Smart.Campus.demo.service;

import Smart.Campus.demo.dto.BookingDto;
import Smart.Campus.demo.entity.Booking;
import Smart.Campus.demo.entity.Resource;
import Smart.Campus.demo.entity.User;
import Smart.Campus.demo.exception.ConflictException;
import Smart.Campus.demo.exception.ResourceNotFoundException;
import Smart.Campus.demo.exception.UnauthorizedException;
import Smart.Campus.demo.mapper.EntityMapper;
import Smart.Campus.demo.repository.BookingRepository;
import Smart.Campus.demo.repository.ResourceRepository;
import Smart.Campus.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for Booking Management (Module B).
 * Handles booking creation with conflict detection, and admin review workflow.
 * Workflow: PENDING → APPROVED/REJECTED. Approved → CANCELLED.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    /**
     * Get all bookings with optional filters.
     * Regular users only see their own; admins see all.
     */
    @Transactional(readOnly = true)
    public List<BookingDto.Response> getBookings(Long userId, Long resourceId, String status, boolean isAdmin) {
        List<Booking> bookings;

        if (isAdmin) {
            // Admin can see all bookings, optionally filtered
            bookings = bookingRepository.findByFilters(userId, resourceId, status);
        } else {
            // Regular users can only see their own bookings
            bookings = bookingRepository.findByFilters(userId, resourceId, status);
        }

        return bookings.stream()
                .map(EntityMapper::toBookingResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get a single booking by ID.
     */
    @Transactional(readOnly = true)
    public BookingDto.Response getBookingById(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + bookingId));
        return EntityMapper.toBookingResponse(booking);
    }

    /**
     * Create a new booking request.
     * Validates: resource exists, resource is ACTIVE, no time conflicts, end > start.
     */
    public BookingDto.Response createBooking(Long userId, BookingDto.CreateRequest request) {
        // Validate the user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        // Validate the resource exists
        Resource resource = resourceRepository.findById(request.getResourceId())
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found with ID: " + request.getResourceId()));

        // Check resource is active
        if ("OUT_OF_SERVICE".equals(resource.getStatus())) {
            throw new IllegalArgumentException("Cannot book a resource that is OUT_OF_SERVICE");
        }

        // Validate time range
        if (request.getEndTime().isBefore(request.getStartTime()) || request.getEndTime().isEqual(request.getStartTime())) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        // Check for scheduling conflicts
        List<Booking> conflicts = bookingRepository.findConflictingBookings(
                request.getResourceId(), request.getStartTime(), request.getEndTime());
        if (!conflicts.isEmpty()) {
            throw new ConflictException("Scheduling conflict: the resource is already booked for the requested time range");
        }

        // Create the booking with PENDING status
        Booking booking = Booking.builder()
                .user(user)
                .resource(resource)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .purpose(request.getPurpose())
                .status("PENDING")
                .build();

        Booking saved = bookingRepository.save(booking);
        return EntityMapper.toBookingResponse(saved);
    }

    /**
     * Approve a booking (ADMIN only).
     * Checks for conflicts again before approving to prevent race conditions.
     */
    public BookingDto.Response approveBooking(Long bookingId, BookingDto.ReviewRequest request) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + bookingId));

        if (!"PENDING".equals(booking.getStatus())) {
            throw new IllegalArgumentException("Only PENDING bookings can be approved. Current status: " + booking.getStatus());
        }

        // Re-check for conflicts before approving
        List<Booking> conflicts = bookingRepository.findConflictingBookings(
                booking.getResource().getResourceId(), booking.getStartTime(), booking.getEndTime());
        if (!conflicts.isEmpty()) {
            throw new ConflictException("Cannot approve: scheduling conflict detected with another approved booking");
        }

        booking.setStatus("APPROVED");
        if (request != null && request.getAdminReason() != null) {
            booking.setAdminReason(request.getAdminReason());
        }

        Booking updated = bookingRepository.save(booking);

        // Send notification to the user
        notificationService.createNotification(booking.getUser(),
                "Your booking for '" + booking.getResource().getName() + "' has been APPROVED.");

        return EntityMapper.toBookingResponse(updated);
    }

    /**
     * Reject a booking (ADMIN only).
     */
    public BookingDto.Response rejectBooking(Long bookingId, BookingDto.ReviewRequest request) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + bookingId));

        if (!"PENDING".equals(booking.getStatus())) {
            throw new IllegalArgumentException("Only PENDING bookings can be rejected. Current status: " + booking.getStatus());
        }

        booking.setStatus("REJECTED");
        if (request != null && request.getAdminReason() != null) {
            booking.setAdminReason(request.getAdminReason());
        }

        Booking updated = bookingRepository.save(booking);

        // Send notification to the user
        String reason = request != null && request.getAdminReason() != null
                ? " Reason: " + request.getAdminReason() : "";
        notificationService.createNotification(booking.getUser(),
                "Your booking for '" + booking.getResource().getName() + "' has been REJECTED." + reason);

        return EntityMapper.toBookingResponse(updated);
    }

    /**
     * Cancel a booking (by the booking owner).
     * Only APPROVED or PENDING bookings can be cancelled.
     */
    public BookingDto.Response cancelBooking(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + bookingId));

        if (!booking.getUser().getUserId().equals(userId)) {
            throw new UnauthorizedException("You can only cancel your own bookings");
        }

        if (!"PENDING".equals(booking.getStatus()) && !"APPROVED".equals(booking.getStatus())) {
            throw new IllegalArgumentException("Only PENDING or APPROVED bookings can be cancelled. Current status: " + booking.getStatus());
        }

        booking.setStatus("CANCELLED");
        Booking updated = bookingRepository.save(booking);
        return EntityMapper.toBookingResponse(updated);
    }

    /**
     * Delete a booking.
     */
    public void deleteBooking(Long bookingId) {
        if (!bookingRepository.existsById(bookingId)) {
            throw new ResourceNotFoundException("Booking not found with ID: " + bookingId);
        }
        bookingRepository.deleteById(bookingId);
    }
}
