package com.getset.property.api;

import com.getset.common.dto.PageResponse;
import com.getset.common.exception.ForbiddenException;
import com.getset.common.exception.NotFoundException;
import com.getset.property.domain.PropertyDocument;
import com.getset.property.domain.PropertyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    /**
     * GET /api/v1/properties
     * Supports optional query params: city, propertyType, minPrice, maxPrice, bedrooms, keyword, sort
     * e.g. ?city=Mumbai&propertyType=APARTMENT&minPrice=10000&maxPrice=50000&bedrooms=2&keyword=sea view&sort=pricePerMonth,asc
     */
    @GetMapping
    public ResponseEntity<PageResponse<PropertyDocument>> getAllProperties(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String propertyType,
            @RequestParam(required = false, defaultValue = "0") double minPrice,
            @RequestParam(required = false, defaultValue = "999999999") double maxPrice,
            @RequestParam(required = false, defaultValue = "0") int bedrooms,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortDir,
            Pageable pageable) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        Page<PropertyDocument> page;

        boolean hasFilters = (city != null && !city.isBlank())
                || (propertyType != null && !propertyType.isBlank())
                || minPrice > 0 || maxPrice < 999999999
                || bedrooms > 0
                || (keyword != null && !keyword.isBlank());

        if (hasFilters) {
            page = propertyRepository.searchWithFilters(
                    (city != null && !city.isBlank()) ? city : null,
                    (propertyType != null && !propertyType.isBlank()) ? propertyType : null,
                    minPrice,
                    maxPrice,
                    bedrooms,
                    (keyword != null && !keyword.isBlank()) ? keyword : null,
                    sortedPageable
            );
        } else {
            page = propertyRepository.findAll(sortedPageable);
        }

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

        // Map only editable fields to preserve other data like imageUrls, location,
        // createdAt
        if (request.getTitle() != null)
            existing.setTitle(request.getTitle());
        if (request.getDescription() != null)
            existing.setDescription(request.getDescription());
        if (request.getPricePerMonth() > 0)
            existing.setPricePerMonth(request.getPricePerMonth());
        if (request.getBedrooms() >= 0)
            existing.setBedrooms(request.getBedrooms());
        if (request.getBathrooms() >= 0)
            existing.setBathrooms(request.getBathrooms());
        if (request.getAreaSqFt() > 0)
            existing.setAreaSqFt(request.getAreaSqFt());
        if (request.getCity() != null)
            existing.setCity(request.getCity());
        // Since address isn't a direct field but often mapped to fullAddress/state/etc
        // in AddProperty
        if (request.getAmenities() != null)
            existing.setAmenities(request.getAmenities());

        return ResponseEntity.ok(propertyRepository.save(existing));
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
