package com.getset.property;

import com.getset.constant.AppConstants;
import com.getset.property.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import com.getset.common.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "Properties", description = "Property management endpoints")
@RequestMapping(AppConstants.API_BASE_PATH + "/properties")
@RequiredArgsConstructor
public class PropertyController {

    private final PropertyService propertyService;

    @PostMapping
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "Create a new property")
    public ResponseEntity<PropertyResponse> createProperty(
            @Valid @RequestBody PropertyCreateRequest request,
            Authentication authentication) {
        PropertyResponse response = propertyService.createProperty(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "Update an existing property")
    public ResponseEntity<PropertyResponse> updateProperty(
            @PathVariable String id,
            @Valid @RequestBody PropertyUpdateRequest request,
            Authentication authentication) {
        PropertyResponse response = propertyService.updateProperty(id, request, authentication.getName());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "Deactivate a property")
    public ResponseEntity<Void> deleteProperty(
            @PathVariable String id,
            Authentication authentication) {
        propertyService.deactivateProperty(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get property by ID")
    public ResponseEntity<PropertyResponse> getProperty(@PathVariable String id) {
        PropertyResponse response = propertyService.getProperty(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Search properties with pagination and filters")
    public ResponseEntity<PageResponse<PropertySummaryResponse>> searchProperties(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Integer minBedrooms,
            @RequestParam(required = false) Boolean furnished,
            @RequestParam(required = false) PropertyType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PageResponse<PropertySummaryResponse> results = propertyService.searchProperties(
                city, minPrice, maxPrice, minBedrooms, furnished, type, page, size);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/nearby")
    @Operation(summary = "Find nearby properties")
    public ResponseEntity<List<PropertySummaryResponse>> findNearby(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "5") double radiusKm) {

        List<PropertySummaryResponse> results = propertyService.findNearby(lat, lng, radiusKm);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/owner/my-properties")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "Get current owner's properties")
    public ResponseEntity<List<PropertySummaryResponse>> getMyProperties(Authentication authentication) {
        List<PropertySummaryResponse> results = propertyService.getOwnerProperties(authentication.getName());
        return ResponseEntity.ok(results);
    }
}
