package Smart.Campus.demo.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * TicketAttachment entity for storing image evidence on incident tickets.
 * Each ticket can have up to 3 attachments.
 */
@Entity
@Table(name = "ticket_attachments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attachment_id")
    private Long attachmentId;

    /** The ticket this attachment belongs to */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private IncidentTicket ticket;

    @Column(name = "file_url", nullable = false, length = 500)
    private String fileUrl;
}
