package Smart.Campus.demo.service;

import Smart.Campus.demo.dto.ResourceDto;
import Smart.Campus.demo.entity.Resource;
import Smart.Campus.demo.exception.ResourceNotFoundException;
import Smart.Campus.demo.mapper.EntityMapper;
import Smart.Campus.demo.repository.ResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for Resource (Module A - Facilities & Assets Catalogue).
 * Handles CRUD operations and filtered search for campus resources.
 */
@Service
@RequiredArgsConstructor
@Transactional
/**
 * @author mohsh
 */
public class ResourceService {

    private final ResourceRepository resourceRepository;

    /**
     * Get all resources with optional filters.
     */
    @Transactional(readOnly = true)
    public List<ResourceDto.Response> getAllResources(String type, String status, String location, Integer minCapacity) {
        List<Resource> resources;

        // If any filter is provided, use filtered query; otherwise return all
        if (type != null || status != null || location != null || minCapacity != null) {
            resources = resourceRepository.findByFilters(type, status, location, minCapacity);
        } else {
            resources = resourceRepository.findAll();
        }

        return resources.stream()
                .map(EntityMapper::toResourceResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get a single resource by its ID.
     * @throws ResourceNotFoundException if resource doesn't exist
     */
    @Transactional(readOnly = true)
    public ResourceDto.Response getResourceById(Long id) {
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found with ID: " + id));
        return EntityMapper.toResourceResponse(resource);
    }

    /**
     * Create a new resource (ADMIN only).
     */
    public ResourceDto.Response createResource(ResourceDto.Request request) {
        Resource resource = EntityMapper.toResourceEntity(request);
        Resource saved = resourceRepository.save(resource);
        return EntityMapper.toResourceResponse(saved);
    }

    /**
     * Update an existing resource (ADMIN only).
     * @throws ResourceNotFoundException if resource doesn't exist
     */
    public ResourceDto.Response updateResource(Long id, ResourceDto.Request request) {
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found with ID: " + id));

        resource.setName(request.getName());
        resource.setType(request.getType().toUpperCase());
        resource.setCapacity(request.getCapacity());
        resource.setLocation(request.getLocation());
        if (request.getStatus() != null) {
            resource.setStatus(request.getStatus().toUpperCase());
        }

        Resource updated = resourceRepository.save(resource);
        return EntityMapper.toResourceResponse(updated);
    }

    /**
     * Delete a resource by ID (ADMIN only).
     * @throws ResourceNotFoundException if resource doesn't exist
     */
    public void deleteResource(Long id) {
        if (!resourceRepository.existsById(id)) {
            throw new ResourceNotFoundException("Resource not found with ID: " + id);
        }
        resourceRepository.deleteById(id);
    }
}
