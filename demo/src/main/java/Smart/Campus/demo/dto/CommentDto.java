package Smart.Campus.demo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

/**
 * DTOs for Comment-related operations (Module C sub-feature).
 */
public class CommentDto {

    /** Request DTO for creating/updating a comment */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        @NotBlank(message = "Comment content is required")
        private String content;
    }

    /** Response DTO */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long commentId;
        private UserDto.Response user;
        private String content;
        private LocalDateTime createdAt;
    }
}
