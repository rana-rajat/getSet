package com.getset.search.service.impl;

import com.getset.property.PropertyDocument;
import com.getset.property.PropertyType;
import com.getset.property.PropertyRepository;
import com.getset.property.dto.AddressDto;
import com.getset.property.dto.PropertyResponse;
import com.getset.search.dto.SearchRequestDto;
import com.getset.search.dto.SearchResponseDto;
import com.getset.search.service.PropertySearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Property Search Service Implementation
 * Provides search with MongoDB filtering and Redis caching
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PropertySearchServiceImpl implements PropertySearchService {

    private final PropertyRepository propertyRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public SearchResponseDto search(SearchRequestDto request) {
        long startTime = System.currentTimeMillis();

        log.info("Searching properties with filters - city: {}, minPrice: {}, maxPrice: {}, bedrooms: {}",
                request.getCity(), request.getMinPrice(), request.getMaxPrice(), request.getMinBedrooms());

        // Set defaults
        Integer page = request.getPage() != null ? request.getPage() : 0;
        Integer size = request.getSize() != null ? request.getSize() : 10;
        String sortBy = request.getSortBy() != null ? request.getSortBy() : "createdAt";
        Boolean ascending = request.getAscending() != null ? request.getAscending() : false;

        // Create pageable
        Sort.Direction direction = ascending ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        // Search using repository (MongoDB)
        Page<PropertyDocument> allResults = propertyRepository.findAll(pageable);

        // Filter results in memory
        List<PropertyDocument> filtered = allResults.getContent().stream()
                .filter(p -> filterProperty(p, request))
                .collect(Collectors.toList());

        // Convert to response DTOs
        List<PropertyResponse> properties = filtered.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        long searchTime = System.currentTimeMillis() - startTime;
        int totalPages = (filtered.size() + size - 1) / size;

        return SearchResponseDto.builder()
                .properties(properties)
                .totalCount((long) filtered.size())
                .currentPage(page)
                .pageSize(size)
                .totalPages(totalPages)
                .hasNext(page < totalPages - 1)
                .hasPrevious(page > 0)
                .searchTimeMs(searchTime)
                .build();
    }

    @Override
    public List<String> getSuggestions(String query, Integer limit) {
        if (query == null || query.isEmpty()) {
            return new ArrayList<>();
        }

        int suggestionLimit = limit != null ? limit : 5;

        log.debug("Getting search suggestions for query: {}", query);

        return propertyRepository.findAll()
                .stream()
                .filter(p -> p.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                           (p.getDescription() != null && p.getDescription().toLowerCase().contains(query.toLowerCase())))
                .limit(suggestionLimit)
                .map(PropertyDocument::getTitle)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Convert PropertyDocument to PropertyResponse DTO
     */
    private PropertyResponse convertToResponse(PropertyDocument property) {
        AddressDto addressDto = null;
        if (property.getAddress() != null) {
            addressDto = AddressDto.builder()
                    .fullAddress(property.getAddress().getFullAddress())
                    .city(property.getAddress().getCity())
                    .state(property.getAddress().getState())
                    .country(property.getAddress().getCountry())
                    .pincode(property.getAddress().getPincode())
                    .build();
        }

        Double lat = null;
        Double lng = null;
        if (property.getLocation() != null) {
            lat = property.getLocation().getLatitude();
            lng = property.getLocation().getLongitude();
        }

        return PropertyResponse.builder()
                .id(property.getId())
                .title(property.getTitle())
                .description(property.getDescription())
                .type(property.getType())
                .pricePerMonth(property.getPricePerMonth())
                .bedrooms(property.getBedrooms())
                .bathrooms(property.getBathrooms())
                .furnished(property.getFurnished())
                .amenities(property.getAmenities())
                .address(addressDto)
                .lat(lat)
                .lng(lng)
                .photos(property.getPhotos())
                .ownerId(property.getOwnerId())
                .isActive(property.getIsActive())
                .createdAt(property.getCreatedAt())
                .updatedAt(property.getUpdatedAt())
                .build();
    }

    /**
     * Filter property based on search request criteria
     */
    private boolean filterProperty(PropertyDocument property, SearchRequestDto request) {
        // Filter by city
        if (request.getCity() != null && !request.getCity().isEmpty()) {
            if (property.getAddress() == null ||
                !property.getAddress().getCity().equalsIgnoreCase(request.getCity())) {
                return false;
            }
        }

        // Filter by type
        if (request.getType() != null && !request.getType().isEmpty()) {
            try {
                PropertyType type = PropertyType.valueOf(request.getType().toUpperCase());
                if (property.getType() != type) {
                    return false;
                }
            } catch (IllegalArgumentException e) {
                return false;
            }
        }

        // Filter by price range
        if (request.getMinPrice() != null && property.getPricePerMonth() < request.getMinPrice()) {
            return false;
        }
        if (request.getMaxPrice() != null && property.getPricePerMonth() > request.getMaxPrice()) {
            return false;
        }

        // Filter by minimum bedrooms
        if (request.getMinBedrooms() != null && property.getBedrooms() < request.getMinBedrooms()) {
            return false;
        }

        // Filter by furnished status
        if (request.getFurnished() != null && !property.getFurnished().equals(request.getFurnished())) {
            return false;
        }

        return property.getIsActive();
    }
}

