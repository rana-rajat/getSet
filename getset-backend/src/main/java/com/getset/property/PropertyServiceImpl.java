package com.getset.property;

import com.getset.common.NotFoundException;
import com.getset.property.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PropertyServiceImpl implements PropertyService {
    
    private final PropertyRepository propertyRepository;
    private final PropertyMapper propertyMapper;

    @Override
    public PropertyResponse createProperty(PropertyCreateRequest request, String ownerId) {
        PropertyDocument doc = propertyMapper.toDocument(request, ownerId);
        propertyRepository.save(doc);
        return propertyMapper.toResponse(doc);
    }

    @Override
    public PropertyResponse updateProperty(String id, PropertyUpdateRequest request, String ownerId) {
        PropertyDocument doc = propertyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Property not found"));
        
        if (!doc.getOwnerId().equals(ownerId)) {
            throw new NotFoundException("Not authorized to update this property");
        }
        
        if (request.getTitle() != null) doc.setTitle(request.getTitle());
        if (request.getDescription() != null) doc.setDescription(request.getDescription());
        if (request.getType() != null) doc.setType(request.getType());
        if (request.getPricePerMonth() != null) doc.setPricePerMonth(request.getPricePerMonth());
        if (request.getBedrooms() != null) doc.setBedrooms(request.getBedrooms());
        if (request.getBathrooms() != null) doc.setBathrooms(request.getBathrooms());
        if (request.getFurnished() != null) doc.setFurnished(request.getFurnished());
        if (request.getAmenities() != null) doc.setAmenities(request.getAmenities());
        
        if (request.getAddress() != null) {
            doc.setAddress(Address.builder()
                    .fullAddress(request.getAddress().getFullAddress())
                    .city(request.getAddress().getCity())
                    .state(request.getAddress().getState())
                    .country(request.getAddress().getCountry())
                    .pincode(request.getAddress().getPincode())
                    .build());
        }
        
        if (request.getLat() != null && request.getLng() != null) {
            doc.setLocation(Location.fromCoordinates(request.getLng(), request.getLat()));
        }
        
        if (request.getPhotos() != null) doc.setPhotos(request.getPhotos());
        if (request.getIsActive() != null) doc.setIsActive(request.getIsActive());
        
        propertyRepository.save(doc);
        return propertyMapper.toResponse(doc);
    }

    @Override
    public void deactivateProperty(String id, String ownerId) {
        PropertyDocument doc = propertyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Property not found"));
        
        if (!doc.getOwnerId().equals(ownerId)) {
            throw new NotFoundException("Not authorized to delete this property");
        }
        
        doc.setIsActive(false);
        propertyRepository.save(doc);
    }

    @Override
    public PropertyResponse getProperty(String id) {
        PropertyDocument doc = propertyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Property not found"));
        return propertyMapper.toResponse(doc);
    }

    @Override
    public List<PropertySummaryResponse> searchProperties(
            String city,
            Double minPrice,
            Double maxPrice,
            Integer minBedrooms,
            Boolean furnished,
            PropertyType type,
            int page,
            int size) {
        
        List<PropertyDocument> docs = propertyRepository.searchProperties(
                city, minPrice, maxPrice, minBedrooms, furnished, type, page, size);
        return docs.stream()
                .map(propertyMapper::toSummary)
                .collect(Collectors.toList());
    }

    @Override
    public List<PropertySummaryResponse> findNearby(double lat, double lng, double radiusKm) {
        double radiusMeters = radiusKm * 1000;
        List<PropertyDocument> docs = propertyRepository.findNearby(lng, lat, radiusMeters);
        return docs.stream()
                .map(propertyMapper::toSummary)
                .collect(Collectors.toList());
    }

    @Override
    public List<PropertySummaryResponse> getOwnerProperties(String ownerId) {
        List<PropertyDocument> docs = propertyRepository.findByOwnerIdAndIsActive(ownerId, true);
        return docs.stream()
                .map(propertyMapper::toSummary)
                .collect(Collectors.toList());
    }
}
