package com.getset.property.dto;

import com.getset.property.PropertyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertySummaryResponse {
    
    private String id;
    private String title;
    private PropertyType type;
    private Double pricePerMonth;
    private String city;
    private String thumbnail;
    private Boolean furnished;
    private Integer bedrooms;
}
