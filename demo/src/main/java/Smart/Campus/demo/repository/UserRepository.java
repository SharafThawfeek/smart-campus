package Smart.Campus.demo.repository;

import Smart.Campus.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for User entity.
 * Provides CRUD operations and custom queries for user management.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /** Find a user by their email (used during OAuth login) */
    Optional<User> findByEmail(String email);

    /** Check if a user exists by email */
    boolean existsByEmail(String email);

    /** Find all users with a specific role (e.g., TECHNICIAN for assignment) */
    List<User> findByRole(String role);
}
