package com.getset.search.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Search Request DTO
 * Encapsulates search parameters for full-text and filtered search
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchRequestDto {
    private String text;
    private String city;
    private String type;
    private Double minPrice;
    private Double maxPrice;
    private Integer minBedrooms;
    private Boolean furnished;
    private Integer page;
    private Integer size;
    private String sortBy;
    private Boolean ascending;
}
