package Smart.Campus.demo.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * User entity representing platform users.
 * Supports roles: USER, ADMIN, TECHNICIAN.
 * Linked to Google OAuth2 for authentication.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, length = 50)
    private String role; // USER, ADMIN, TECHNICIAN

    @Column(name = "profile_picture", length = 500)
    private String profilePicture;
}
