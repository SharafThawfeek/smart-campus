package Smart.Campus.demo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * DTOs for User-related operations.
 */
public class UserDto {

    /** Response DTO - what the API returns */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long userId;
        private String email;
        private String name;
        private String role;
        private String profilePicture;
    }

    /** Request DTO for updating user role */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoleUpdateRequest {
        @NotBlank(message = "Role is required")
        private String role;
    }

    /** Request DTO for Google OAuth login */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GoogleLoginRequest {
        @NotBlank(message = "Google credential token is required")
        private String credential;
    }

    /** Response DTO for authentication (includes JWT token) */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AuthResponse {
        private String token;
        private Response user;
    }
}
