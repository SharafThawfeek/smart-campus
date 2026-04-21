package Smart.Campus.demo.repository;

import Smart.Campus.demo.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Comment entity.
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    /** Find all comments for a specific ticket, ordered by creation time */
    List<Comment> findByTicketTicketIdOrderByCreatedAtAsc(Long ticketId);

    /** Find comments by a specific user */
    List<Comment> findByUserUserId(Long userId);
}
