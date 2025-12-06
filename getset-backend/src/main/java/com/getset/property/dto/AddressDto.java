package com.getset.property.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressDto {
    
    @NotBlank(message = "Full address is required")
    private String fullAddress;
    
    @NotBlank(message = "City is required")
    private String city;
    
    @NotBlank(message = "State is required")
    private String state;
    
    @NotBlank(message = "Country is required")
    private String country;
    
    @NotBlank(message = "Pincode is required")
    private String pincode;
}
