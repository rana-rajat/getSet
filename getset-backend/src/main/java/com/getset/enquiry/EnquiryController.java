package com.getset.enquiry;

import com.getset.common.ApiError;
import com.getset.common.PageResponse;
import com.getset.enquiry.dto.EnquiryRequest;
import com.getset.enquiry.dto.EnquiryResponse;
import com.getset.enquiry.dto.EnquiryUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/enquiries")
@RequiredArgsConstructor
public class EnquiryController {
    
    private final EnquiryService enquiryService;
    
    /**
     * Create a new enquiry for a property
     */
    @PostMapping
    @PreAuthorize("hasRole('RENTER')")
    public ResponseEntity<EnquiryResponse> createEnquiry(
            @Valid @RequestBody EnquiryRequest request,
            @RequestHeader("X-User-Id") String renterId) {
        EnquiryResponse response = enquiryService.createEnquiry(request, renterId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Get enquiry by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<EnquiryResponse> getEnquiry(@PathVariable String id) {
        EnquiryResponse response = enquiryService.getEnquiryById(id);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get all enquiries for a property (owner view)
     */
    @GetMapping("/property/{propertyId}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<PageResponse<EnquiryResponse>> getPropertyEnquiries(
            @PathVariable String propertyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader("X-User-Id") String ownerId) {
        Pageable pageable = PageRequest.of(page, size);
        PageResponse<EnquiryResponse> response = enquiryService.getEnquiriesByPropertyId(propertyId, ownerId, pageable);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get all enquiries sent by a renter
     */
    @GetMapping("/renter/my-enquiries")
    @PreAuthorize("hasRole('RENTER')")
    public ResponseEntity<PageResponse<EnquiryResponse>> getMyEnquiries(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader("X-User-Id") String renterId) {
        Pageable pageable = PageRequest.of(page, size);
        PageResponse<EnquiryResponse> response = enquiryService.getEnquiriesByRenterId(renterId, pageable);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Update enquiry status (owner can accept/reject)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<EnquiryResponse> updateEnquiry(
            @PathVariable String id,
            @Valid @RequestBody EnquiryUpdateRequest request,
            @RequestHeader("X-User-Id") String ownerId) {
        EnquiryResponse response = enquiryService.updateEnquiry(id, request, ownerId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Delete/Cancel an enquiry
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteEnquiry(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId) {
        enquiryService.deleteEnquiry(id, userId);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Get enquiry statistics for owner
     */
    @GetMapping("/owner/stats")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<?> getEnquiryStats(
            @RequestHeader("X-User-Id") String ownerId) {
        return ResponseEntity.ok(enquiryService.getEnquiryStats(ownerId));
    }
}
