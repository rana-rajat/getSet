package com.getset.favorite;

import com.getset.common.PageResponse;
import com.getset.favorite.dto.FavoriteRequest;
import com.getset.favorite.dto.FavoriteResponse;
import org.springframework.data.domain.Pageable;

public interface FavoriteService {
    
    /**
     * Add property to favorites
     */
    FavoriteResponse addFavorite(FavoriteRequest request, String renterId);
    
    /**
     * Remove property from favorites
     */
    void removeFavorite(String propertyId, String renterId);
    
    /**
     * Get all favorites for a renter
     */
    PageResponse<FavoriteResponse> getFavorites(String renterId, Pageable pageable);
    
    /**
     * Check if property is in favorites
     */
    boolean isFavorite(String propertyId, String renterId);
    
    /**
     * Get favorite count for a renter
     */
    long getFavoriteCount(String renterId);
    
    /**
     * Update favorite notes
     */
    FavoriteResponse updateFavoriteNotes(String propertyId, String notes, String renterId);
}
