package com.getset.enquiry;

import com.getset.common.PageResponse;
import com.getset.enquiry.dto.EnquiryRequest;
import com.getset.enquiry.dto.EnquiryResponse;
import com.getset.enquiry.dto.EnquiryUpdateRequest;
import org.springframework.data.domain.Pageable;

public interface EnquiryService {
    
    /**
     * Create a new enquiry for a property
     */
    EnquiryResponse createEnquiry(EnquiryRequest request, String renterId);
    
    /**
     * Get enquiry by ID
     */
    EnquiryResponse getEnquiryById(String id);
    
    /**
     * Get all enquiries for a property
     */
    PageResponse<EnquiryResponse> getEnquiriesByPropertyId(String propertyId, String ownerId, Pageable pageable);
    
    /**
     * Get all enquiries sent by a renter
     */
    PageResponse<EnquiryResponse> getEnquiriesByRenterId(String renterId, Pageable pageable);
    
    /**
     * Update enquiry status
     */
    EnquiryResponse updateEnquiry(String id, EnquiryUpdateRequest request, String ownerId);
    
    /**
     * Delete/Cancel an enquiry
     */
    void deleteEnquiry(String id, String userId);
    
    /**
     * Get enquiry statistics for owner
     */
    Object getEnquiryStats(String ownerId);
}
