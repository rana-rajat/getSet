package com.getset.property.api;

import com.getset.common.dto.PageResponse;
import com.getset.common.exception.ForbiddenException;
import com.getset.common.exception.NotFoundException;
import com.getset.property.domain.PropertyDocument;
import com.getset.property.domain.PropertyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/properties")
@RequiredArgsConstructor
public class PropertyController {

    private final PropertyRepository propertyRepository;

    @GetMapping
    public ResponseEntity<PageResponse<PropertyDocument>> getAllProperties(Pageable pageable) {
        Page<PropertyDocument> page = propertyRepository.findAll(pageable);
        return ResponseEntity.ok(PageResponse.<PropertyDocument>builder()
                .content(page.getContent())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .hasNext(!page.isLast())
                .hasPrevious(page.getNumber() > 0)
                .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PropertyDocument> getById(@PathVariable String id) {
        return ResponseEntity.ok(propertyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Property not found: " + id)));
    }

    @PostMapping
    public ResponseEntity<PropertyDocument> create(
            @RequestBody PropertyDocument request,
            @AuthenticationPrincipal String username) {
        request.setOwnerId(username);
        request.setAvailable(true);
        PropertyDocument saved = propertyRepository.save(request);
        log.info("Property created: {}", saved.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PropertyDocument> update(
            @PathVariable String id,
            @RequestBody PropertyDocument request,
            @AuthenticationPrincipal String username) {
        PropertyDocument existing = propertyRepository.findByIdAndOwnerId(id, username)
                .orElseThrow(() -> new ForbiddenException("You don't own this property"));
        request.setId(existing.getId());
        request.setOwnerId(existing.getOwnerId());
        return ResponseEntity.ok(propertyRepository.save(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(
            @PathVariable String id,
            @AuthenticationPrincipal String username) {
        propertyRepository.findByIdAndOwnerId(id, username)
                .orElseThrow(() -> new ForbiddenException("You don't own this property"));
        propertyRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Property deleted successfully"));
    }

    @GetMapping("/owner/my-properties")
    public ResponseEntity<PageResponse<PropertyDocument>> getMyProperties(
            @AuthenticationPrincipal String username, Pageable pageable) {
        Page<PropertyDocument> page = propertyRepository.findByOwnerId(username, pageable);
        return ResponseEntity.ok(PageResponse.<PropertyDocument>builder()
                .content(page.getContent())
                .pageNumber(page.getNumber()).pageSize(page.getSize())
                .totalElements(page.getTotalElements()).totalPages(page.getTotalPages())
                .hasNext(!page.isLast()).hasPrevious(page.getNumber() > 0)
                .build());
    }
}
