package Smart.Campus.demo.controller;

import Smart.Campus.demo.dto.BookingDto;
import Smart.Campus.demo.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Booking Management (Module B).
 * Handles booking creation, review (approve/reject), cancellation, and queries.
 * 
 * Base path: /api/v1/bookings
 * 
 * Workflow: PENDING → APPROVED/REJECTED → CANCELLED
 */
@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    /**
     * Get all bookings with optional filters.
     * Regular users see only their own bookings; admins see all.
     * 
     * GET /api/v1/bookings?resourceId=1&status=PENDING
     * Status: 200 OK
     */
    @GetMapping
    public ResponseEntity<List<BookingDto.Response>> getBookings(
            @RequestParam(required = false) Long resourceId,
            @RequestParam(required = false) String status,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        boolean isAdmin = isAdmin(authentication);

        List<BookingDto.Response> bookings = bookingService.getBookings(
                isAdmin ? null : userId, resourceId, status, isAdmin);
        return ResponseEntity.ok(bookings);
    }

    /**
     * Get a single booking by ID.
     * 
     * GET /api/v1/bookings/{id}
     * Status: 200 OK | 404 Not Found
     */
    @GetMapping("/{id}")
    public ResponseEntity<BookingDto.Response> getBookingById(@PathVariable Long id) {
        BookingDto.Response booking = bookingService.getBookingById(id);
        return ResponseEntity.ok(booking);
    }

    /**
     * Create a new booking request.
     * 
     * POST /api/v1/bookings
     * Status: 201 Created | 400 Bad Request | 409 Conflict
     */
    @PostMapping
    public ResponseEntity<BookingDto.Response> createBooking(
            @Valid @RequestBody BookingDto.CreateRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        BookingDto.Response created = bookingService.createBooking(userId, request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    /**
     * Approve a booking (ADMIN only).
     * 
     * PUT /api/v1/bookings/{id}/approve
     * Status: 200 OK | 404 Not Found | 400 Bad Request | 409 Conflict
     */
    @PutMapping("/{id}/approve")
    public ResponseEntity<BookingDto.Response> approveBooking(
            @PathVariable Long id,
            @RequestBody(required = false) BookingDto.ReviewRequest request) {
        BookingDto.Response approved = bookingService.approveBooking(id, request);
        return ResponseEntity.ok(approved);
    }

    /**
     * Reject a booking (ADMIN only).
     * 
     * PUT /api/v1/bookings/{id}/reject
     * Status: 200 OK | 404 Not Found | 400 Bad Request
     */
    @PutMapping("/{id}/reject")
    public ResponseEntity<BookingDto.Response> rejectBooking(
            @PathVariable Long id,
            @RequestBody(required = false) BookingDto.ReviewRequest request) {
        BookingDto.Response rejected = bookingService.rejectBooking(id, request);
        return ResponseEntity.ok(rejected);
    }

    /**
     * Cancel a booking (owner only).
     * 
     * PUT /api/v1/bookings/{id}/cancel
     * Status: 200 OK | 404 Not Found | 403 Forbidden
     */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<BookingDto.Response> cancelBooking(
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        BookingDto.Response cancelled = bookingService.cancelBooking(id, userId);
        return ResponseEntity.ok(cancelled);
    }

    /**
     * Delete a booking.
     * 
     * DELETE /api/v1/bookings/{id}
     * Status: 204 No Content | 404 Not Found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBooking(@PathVariable Long id) {
        bookingService.deleteBooking(id);
        return ResponseEntity.noContent().build();
    }

    /** Helper to check if the current user has ADMIN role */
    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_ADMIN"));
    }
}
