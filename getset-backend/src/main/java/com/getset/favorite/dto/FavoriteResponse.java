package com.getset.favorite.dto;

import com.getset.property.dto.PropertyResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteResponse {
    
    private String id;
    private String renterId;
    private PropertyResponse property;
    private String notes;
    private Instant createdAt;
}
