package Smart.Campus.demo.repository;

import Smart.Campus.demo.entity.TicketAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for TicketAttachment entity.
 */
@Repository
public interface TicketAttachmentRepository extends JpaRepository<TicketAttachment, Long> {

    /** Find all attachments for a specific ticket */
    List<TicketAttachment> findByTicketTicketId(Long ticketId);
}
