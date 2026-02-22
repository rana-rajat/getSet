package com.getset.search.dto;

import com.getset.property.dto.PropertyResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Search Response DTO
 * Returns search results with pagination info
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchResponseDto {
    private List<PropertyResponse> properties;
    private Long totalCount;
    private Integer currentPage;
    private Integer pageSize;
    private Integer totalPages;
    private Boolean hasNext;
    private Boolean hasPrevious;
    private Long searchTimeMs;
}
