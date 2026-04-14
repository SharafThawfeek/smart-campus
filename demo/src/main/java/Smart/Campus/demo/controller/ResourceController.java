package Smart.Campus.demo.controller;

import Smart.Campus.demo.dto.ResourceDto;
import Smart.Campus.demo.service.ResourceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Facilities & Assets Catalogue (Module A).
 * Provides CRUD operations and filtered search for campus resources.
 * 
 * Base path: /api/v1/resources
 * 
 * Access Control:
 * - GET: All authenticated users
 * - POST/PUT/DELETE: ADMIN only
 */
@RestController
@RequestMapping("/api/v1/resources")
@RequiredArgsConstructor
/**
 * @author mohsh
 */
public class ResourceController {

    private final ResourceService resourceService;

    /**
     * Get all resources with optional filters.
     * 
     * GET /api/v1/resources?type=ROOM&status=ACTIVE&location=Block+A&minCapacity=30
     * Status: 200 OK
     */
    @GetMapping
    public ResponseEntity<List<ResourceDto.Response>> getAllResources(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Integer minCapacity) {
        List<ResourceDto.Response> resources = resourceService.getAllResources(type, status, location, minCapacity);
        return ResponseEntity.ok(resources);
    }

    /**
     * Get a single resource by ID.
     * 
     * GET /api/v1/resources/{id}
     * Status: 200 OK | 404 Not Found
     */
    @GetMapping("/{id}")
    public ResponseEntity<ResourceDto.Response> getResourceById(@PathVariable Long id) {
        ResourceDto.Response resource = resourceService.getResourceById(id);
        return ResponseEntity.ok(resource);
    }

    /**
     * Create a new resource (ADMIN only).
     * 
     * POST /api/v1/resources
     * Status: 201 Created | 400 Bad Request
     */
    @PostMapping
    public ResponseEntity<ResourceDto.Response> createResource(
            @Valid @RequestBody ResourceDto.Request request) {
        ResourceDto.Response created = resourceService.createResource(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    /**
     * Update an existing resource (ADMIN only).
     * 
     * PUT /api/v1/resources/{id}
     * Status: 200 OK | 404 Not Found | 400 Bad Request
     */
    @PutMapping("/{id}")
    public ResponseEntity<ResourceDto.Response> updateResource(
            @PathVariable Long id,
            @Valid @RequestBody ResourceDto.Request request) {
        ResourceDto.Response updated = resourceService.updateResource(id, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * Delete a resource (ADMIN only).
     * 
     * DELETE /api/v1/resources/{id}
     * Status: 204 No Content | 404 Not Found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteResource(@PathVariable Long id) {
        resourceService.deleteResource(id);
        return ResponseEntity.noContent().build();
    }
}
