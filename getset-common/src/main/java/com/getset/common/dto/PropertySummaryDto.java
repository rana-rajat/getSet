package com.getset.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Lightweight property data returned by property-service's internal endpoint.
 * Used by enquiry-service and favorite-service via Feign.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertySummaryDto {
    private String id;
    private String title;
    private String ownerId;
    private String city;
    private double pricePerMonth;
}
