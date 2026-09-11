package com.getset.enquiry.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EnquiryRequest {
    @NotBlank
    private String propertyId;
    @NotBlank
    private String message;
}
