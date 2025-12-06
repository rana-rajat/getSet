package com.getset.property;

import com.getset.property.dto.*;

import java.util.List;

public interface PropertyService {
    
    PropertyResponse createProperty(PropertyCreateRequest request, String ownerId);
    
    PropertyResponse updateProperty(String id, PropertyUpdateRequest request, String ownerId);
    
    void deactivateProperty(String id, String ownerId);
    
    PropertyResponse getProperty(String id);
    
    List<PropertySummaryResponse> searchProperties(
            String city,
            Double minPrice,
            Double maxPrice,
            Integer minBedrooms,
            Boolean furnished,
            PropertyType type,
            int page,
            int size
    );
    
    List<PropertySummaryResponse> findNearby(double lat, double lng, double radiusKm);
    
    List<PropertySummaryResponse> getOwnerProperties(String ownerId);
}
