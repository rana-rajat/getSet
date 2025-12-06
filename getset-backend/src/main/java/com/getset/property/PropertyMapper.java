package com.getset.property;

import com.getset.property.dto.*;
import org.springframework.stereotype.Component;

@Component
public class PropertyMapper {
    
    public PropertyDocument toDocument(PropertyCreateRequest req, String ownerId) {
        return PropertyDocument.builder()
                .ownerId(ownerId)
                .title(req.getTitle())
                .description(req.getDescription())
                .type(req.getType())
                .pricePerMonth(req.getPricePerMonth())
                .bedrooms(req.getBedrooms())
                .bathrooms(req.getBathrooms())
                .furnished(req.getFurnished())
                .amenities(req.getAmenities())
                .address(Address.builder()
                        .fullAddress(req.getAddress().getFullAddress())
                        .city(req.getAddress().getCity())
                        .state(req.getAddress().getState())
                        .country(req.getAddress().getCountry())
                        .pincode(req.getAddress().getPincode())
                        .build())
                .location(Location.fromCoordinates(req.getLng(), req.getLat()))
                .photos(req.getPhotos())
                .isActive(true)
                .build();
    }

    public PropertyResponse toResponse(PropertyDocument doc) {
        return PropertyResponse.builder()
                .id(doc.getId())
                .ownerId(doc.getOwnerId())
                .title(doc.getTitle())
                .description(doc.getDescription())
                .type(doc.getType())
                .pricePerMonth(doc.getPricePerMonth())
                .bedrooms(doc.getBedrooms())
                .bathrooms(doc.getBathrooms())
                .furnished(doc.getFurnished())
                .amenities(doc.getAmenities())
                .address(AddressDto.builder()
                        .fullAddress(doc.getAddress().getFullAddress())
                        .city(doc.getAddress().getCity())
                        .state(doc.getAddress().getState())
                        .country(doc.getAddress().getCountry())
                        .pincode(doc.getAddress().getPincode())
                        .build())
                .lat(doc.getLocation().getLatitude())
                .lng(doc.getLocation().getLongitude())
                .photos(doc.getPhotos())
                .isActive(doc.getIsActive())
                .createdAt(doc.getCreatedAt())
                .updatedAt(doc.getUpdatedAt())
                .build();
    }

    public PropertySummaryResponse toSummary(PropertyDocument doc) {
        return PropertySummaryResponse.builder()
                .id(doc.getId())
                .title(doc.getTitle())
                .type(doc.getType())
                .pricePerMonth(doc.getPricePerMonth())
                .city(doc.getAddress().getCity())
                .thumbnail(doc.getPhotos() != null && !doc.getPhotos().isEmpty() 
                        ? doc.getPhotos().get(0) 
                        : null)
                .furnished(doc.getFurnished())
                .bedrooms(doc.getBedrooms())
                .build();
    }
}
