package com.getset.property.dto;

import com.getset.property.PropertyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyCreateRequest {
    
    @NotBlank(message = "Title is required")
    private String title;
    
    @NotBlank(message = "Description is required")
    private String description;
    
    @NotNull(message = "Property type is required")
    private PropertyType type;
    
    @NotNull(message = "Price per month is required")
    @Positive(message = "Price must be positive")
    private Double pricePerMonth;
    
    @NotNull(message = "Bedrooms is required")
    @Min(value = 1, message = "Bedrooms must be at least 1")
    private Integer bedrooms;
    
    @NotNull(message = "Bathrooms is required")
    @Min(value = 1, message = "Bathrooms must be at least 1")
    private Integer bathrooms;
    
    @NotNull(message = "Furnished status is required")
    private Boolean furnished;
    
    private List<String> amenities;
    
    @NotNull(message = "Address is required")
    private AddressDto address;
    
    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    private Double lat;
    
    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    private Double lng;
    
    private List<String> photos;
}
