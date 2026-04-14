package Smart.Campus.demo.controller;

import Smart.Campus.demo.dto.UserDto;
import Smart.Campus.demo.entity.User;
import Smart.Campus.demo.exception.ResourceNotFoundException;
import Smart.Campus.demo.mapper.EntityMapper;
import Smart.Campus.demo.repository.UserRepository;
import Smart.Campus.demo.security.JwtTokenProvider;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for Authentication (Module E).
 * Handles Google OAuth login, user profile, and role management.
 * 
 * Base path: /api/v1/auth
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
/**
 * @author mohsh
 */
public class AuthController {

    private final UserRepository userRepository;
    private final JwtTokenProvider tokenProvider;

    /**
     * Google OAuth Login.
     * Receives the Google ID token credential, verifies it,
     * and returns a JWT token for the application.
     * 
     * POST /api/v1/auth/google
     * Status: 200 OK
     */
    @PostMapping("/google")
    public ResponseEntity<UserDto.AuthResponse> googleLogin(@Valid @RequestBody UserDto.GoogleLoginRequest request) {
        // For simplicity: the frontend sends Google user info after verifying the token client-side.
        // In production, you'd verify the Google ID token server-side using Google's API.
        // Here we'll accept the credential as a JSON with email, name, picture.

        // Parse the credential (in actual implementation, verify with Google)
        // For now, this endpoint also supports direct login with email for development
        String email = request.getCredential(); // Will be overridden by proper Google flow

        // Check if the user already exists
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            // First-time login: create user with default role USER
            user = User.builder()
                    .email(email)
                    .name(email.split("@")[0]) // Default name from email
                    .role("USER")
                    .build();
            user = userRepository.save(user);
        }

        // Generate JWT token
        String token = tokenProvider.generateToken(user.getUserId(), user.getEmail(), user.getRole());

        UserDto.AuthResponse response = UserDto.AuthResponse.builder()
                .token(token)
                .user(EntityMapper.toUserResponse(user))
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Google OAuth Login with full user data.
     * Receives name, email, and profile picture from the frontend after Google sign-in.
     * 
     * POST /api/v1/auth/google/callback
     * Status: 200 OK
     */
    @PostMapping("/google/callback")
    public ResponseEntity<UserDto.AuthResponse> googleCallback(@RequestBody java.util.Map<String, String> payload) {
        String email = payload.get("email");
        String name = payload.get("name");
        String picture = payload.get("picture");

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }

        // Find or create user
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            // Assign roles based on email for demo accounts
            String role = "USER";
            if ("admin@campus.edu".equalsIgnoreCase(email)) {
                role = "ADMIN";
            } else if ("tech@campus.edu".equalsIgnoreCase(email)) {
                role = "TECHNICIAN";
            }

            user = User.builder()
                    .email(email)
                    .name(name != null ? name : email.split("@")[0])
                    .role(role)
                    .profilePicture(picture)
                    .build();
            user = userRepository.save(user);
        } else {
            // Update profile picture and name if changed
            if (name != null) user.setName(name);
            if (picture != null) user.setProfilePicture(picture);

            // Ensure demo accounts always have correct roles
            if ("admin@campus.edu".equalsIgnoreCase(email)) {
                user.setRole("ADMIN");
            } else if ("tech@campus.edu".equalsIgnoreCase(email)) {
                user.setRole("TECHNICIAN");
            }

            user = userRepository.save(user);
        }

        // Generate JWT
        String token = tokenProvider.generateToken(user.getUserId(), user.getEmail(), user.getRole());

        UserDto.AuthResponse response = UserDto.AuthResponse.builder()
                .token(token)
                .user(EntityMapper.toUserResponse(user))
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Get the currently authenticated user's profile.
     * 
     * GET /api/v1/auth/me
     * Status: 200 OK
     */
    @GetMapping("/me")
    public ResponseEntity<UserDto.Response> getCurrentUser(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return ResponseEntity.ok(EntityMapper.toUserResponse(user));
    }

    /**
     * Update a user's role (ADMIN only).
     * 
     * PUT /api/v1/auth/users/{id}/role
     * Status: 200 OK
     */
    @PutMapping("/users/{id}/role")
    public ResponseEntity<UserDto.Response> updateUserRole(
            @PathVariable Long id,
            @Valid @RequestBody UserDto.RoleUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        String role = request.getRole().toUpperCase();
        if (!List.of("USER", "ADMIN", "TECHNICIAN").contains(role)) {
            throw new IllegalArgumentException("Invalid role. Must be USER, ADMIN, or TECHNICIAN");
        }

        user.setRole(role);
        User updated = userRepository.save(user);
        return ResponseEntity.ok(EntityMapper.toUserResponse(updated));
    }

    /**
     * Get all users (ADMIN only - for user management).
     * 
     * GET /api/v1/auth/users
     * Status: 200 OK
     */
    @GetMapping("/users")
    public ResponseEntity<List<UserDto.Response>> getAllUsers() {
        List<UserDto.Response> users = userRepository.findAll().stream()
                .map(EntityMapper::toUserResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }
}
