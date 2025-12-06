package com.getset.property.dto;

import com.getset.property.PropertyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyResponse {
    
    private String id;
    private String ownerId;
    private String title;
    private String description;
    private PropertyType type;
    private Double pricePerMonth;
    private Integer bedrooms;
    private Integer bathrooms;
    private Boolean furnished;
    private List<String> amenities;
    private AddressDto address;
    private Double lat;
    private Double lng;
    private List<String> photos;
    private Boolean isActive;
    private Instant createdAt;
    private Instant updatedAt;
}
