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

@RestController
@RequestMapping(AppConstants.API_BASE_PATH + "/properties")
@RequiredArgsConstructor
public class PropertyController {
    
    private final PropertyService propertyService;

    @PostMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<PropertyResponse> createProperty(
            @Valid @RequestBody PropertyCreateRequest request,
            Authentication authentication) {
        PropertyResponse response = propertyService.createProperty(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<PropertyResponse> updateProperty(
            @PathVariable String id,
            @Valid @RequestBody PropertyUpdateRequest request,
            Authentication authentication) {
        PropertyResponse response = propertyService.updateProperty(id, request, authentication.getName());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Void> deleteProperty(
            @PathVariable String id,
            Authentication authentication) {
        propertyService.deactivateProperty(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PropertyResponse> getProperty(@PathVariable String id) {
        PropertyResponse response = propertyService.getProperty(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<PropertySummaryResponse>> searchProperties(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Integer minBedrooms,
            @RequestParam(required = false) Boolean furnished,
            @RequestParam(required = false) PropertyType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        List<PropertySummaryResponse> results = propertyService.searchProperties(
                city, minPrice, maxPrice, minBedrooms, furnished, type, page, size);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/nearby")
    public ResponseEntity<List<PropertySummaryResponse>> findNearby(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "5") double radiusKm) {
        
        List<PropertySummaryResponse> results = propertyService.findNearby(lat, lng, radiusKm);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/owner/my-properties")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<List<PropertySummaryResponse>> getMyProperties(Authentication authentication) {
        List<PropertySummaryResponse> results = propertyService.getOwnerProperties(authentication.getName());
        return ResponseEntity.ok(results);
    }
}
