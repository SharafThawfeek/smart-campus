package Smart.Campus.demo.repository;

import Smart.Campus.demo.entity.IncidentTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for IncidentTicket entity.
 * Supports filtering by user, status, priority, and category.
 */
@Repository
public interface IncidentTicketRepository extends JpaRepository<IncidentTicket, Long> {

    /** Find tickets created by a specific user */
    List<IncidentTicket> findByUserUserId(Long userId);

    /** Find tickets by status */
    List<IncidentTicket> findByStatus(String status);

    /** Find tickets by priority */
    List<IncidentTicket> findByPriority(String priority);

    /** Find tickets assigned to a specific technician */
    List<IncidentTicket> findByAssignedTechnicianUserId(Long technicianId);

    /** Filter tickets by multiple criteria */
    @Query("SELECT t FROM IncidentTicket t WHERE " +
           "(:userId IS NULL OR t.user.userId = :userId) AND " +
           "(:status IS NULL OR t.status = :status) AND " +
           "(:priority IS NULL OR t.priority = :priority) AND " +
           "(:category IS NULL OR t.category = :category)")
    List<IncidentTicket> findByFilters(
            @Param("userId") Long userId,
            @Param("status") String status,
            @Param("priority") String priority,
            @Param("category") String category
    );
}
