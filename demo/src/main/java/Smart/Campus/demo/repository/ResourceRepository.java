package Smart.Campus.demo.repository;

import Smart.Campus.demo.entity.Resource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Resource entity.
 * Supports filtering by type, capacity, location, and status.
 */
@Repository
public interface ResourceRepository extends JpaRepository<Resource, Long> {

    /** Find resources by type (ROOM, LAB, EQUIPMENT) */
    List<Resource> findByType(String type);

    /** Find resources by status (ACTIVE, OUT_OF_SERVICE) */
    List<Resource> findByStatus(String status);

    /** Find resources by location (partial match, case-insensitive) */
    List<Resource> findByLocationContainingIgnoreCase(String location);

    /** Find resources with capacity >= given value */
    List<Resource> findByCapacityGreaterThanEqual(Integer capacity);

    /** Advanced filter: search by multiple criteria */
    @Query("SELECT r FROM Resource r WHERE " +
           "(:type IS NULL OR r.type = :type) AND " +
           "(:status IS NULL OR r.status = :status) AND " +
           "(:location IS NULL OR LOWER(r.location) LIKE LOWER(CONCAT('%', :location, '%'))) AND " +
           "(:minCapacity IS NULL OR r.capacity >= :minCapacity)")
    List<Resource> findByFilters(
            @Param("type") String type,
            @Param("status") String status,
            @Param("location") String location,
            @Param("minCapacity") Integer minCapacity
    );
}
