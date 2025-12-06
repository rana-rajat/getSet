package com.getset.enquiry.dto;

import com.getset.enquiry.EnquiryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnquiryRequest {
    
    @NotBlank(message = "Property ID is required")
    private String propertyId;
    
    @NotBlank(message = "Message is required")
    private String message;
}
