package Smart.Campus.demo.dto;

import lombok.*;

/**
 * DTO for API error responses.
 * Provides consistent error format across all endpoints.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiErrorResponse {
    private int status;
    private String error;
    private String message;
    private String timestamp;
}
