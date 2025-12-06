package com.getset.favorite.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteRequest {
    
    private String propertyId;
    private String notes;
}
