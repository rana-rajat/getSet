package com.getset.enquiry.dto;

import com.getset.enquiry.EnquiryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnquiryResponse {
    
    private String id;
    private String propertyId;
    private String renterId;
    private String renterName;
    private String renterEmail;
    private String renterPhone;
    private String ownerId;
    private String ownerName;
    private String message;
    private EnquiryStatus status;
    private String rejectionReason;
    private Instant createdAt;
    private Instant updatedAt;
}
