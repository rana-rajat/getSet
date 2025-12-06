package com.getset.enquiry.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnquiryUpdateRequest {
    
    @NotNull(message = "Status is required")
    private String status;
    
    private String rejectionReason;
}
