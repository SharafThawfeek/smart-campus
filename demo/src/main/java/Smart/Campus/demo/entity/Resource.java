package Smart.Campus.demo.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Resource entity representing bookable campus resources.
 * Types include: ROOM, LAB, EQUIPMENT.
 * Status can be: ACTIVE, OUT_OF_SERVICE.
 */
@Entity
@Table(name = "resources")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Resource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "resource_id")
    private Long resourceId;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, length = 50)
    private String type; // ROOM, LAB, EQUIPMENT

    @Column
    private Integer capacity;

    @Column(length = 255)
    private String location;

    @Column(length = 50)
    @Builder.Default
    private String status = "ACTIVE"; // ACTIVE, OUT_OF_SERVICE
}
