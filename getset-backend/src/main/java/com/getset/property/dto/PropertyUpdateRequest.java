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
public class PropertyUpdateRequest {
    
    private String title;
    private String description;
    private PropertyType type;
    
    @Positive(message = "Price must be positive")
    private Double pricePerMonth;
    
    @Min(value = 1, message = "Bedrooms must be at least 1")
    private Integer bedrooms;
    
    @Min(value = 1, message = "Bathrooms must be at least 1")
    private Integer bathrooms;
    
    private Boolean furnished;
    private List<String> amenities;
    private AddressDto address;
    
    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    private Double lat;
    
    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    private Double lng;
    
    private List<String> photos;
    private Boolean isActive;
}
