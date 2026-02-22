package com.getset.favorite;

import com.getset.common.NotFoundException;
import com.getset.common.PageResponse;
import com.getset.favorite.dto.FavoriteRequest;
import com.getset.favorite.dto.FavoriteResponse;
import com.getset.property.PropertyDocument;
import com.getset.property.PropertyMapper;
import com.getset.property.PropertyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {
    
    private final FavoriteRepository favoriteRepository;
    private final PropertyRepository propertyRepository;
    private final PropertyMapper propertyMapper;
    
    @Override
    public FavoriteResponse addFavorite(FavoriteRequest request, String renterId) {
        log.info("Adding favorite property {} for renter {}", request.getPropertyId(), renterId);
        
        // Verify property exists
        PropertyDocument property = propertyRepository.findById(request.getPropertyId())
                .orElseThrow(() -> new NotFoundException("Property not found with ID: " + request.getPropertyId()));
        
        // Check if already in favorites
        if (favoriteRepository.existsByRenterIdAndPropertyId(renterId, request.getPropertyId())) {
            throw new IllegalArgumentException("Property is already in your favorites");
        }
        
        FavoriteDocument favorite = FavoriteDocument.builder()
                .renterId(renterId)
                .propertyId(request.getPropertyId())
                .notes(request.getNotes())
                .build();
        
        FavoriteDocument saved = favoriteRepository.save(favorite);
        log.info("Favorite added with ID: {}", saved.getId());
        
        return mapToResponse(saved, property);
    }
    
    @Override
    public void removeFavorite(String propertyId, String renterId) {
        log.info("Removing favorite property {} for renter {}", propertyId, renterId);
        
        favoriteRepository.findByRenterIdAndPropertyId(renterId, propertyId)
                .orElseThrow(() -> new NotFoundException("Property not in favorites"));
        
        favoriteRepository.deleteByRenterIdAndPropertyId(renterId, propertyId);
        log.info("Favorite removed for property {}", propertyId);
    }
    
    @Override
    public PageResponse<FavoriteResponse> getFavorites(String renterId, Pageable pageable) {
        log.info("Fetching favorites for renter {}", renterId);
        
        Page<FavoriteDocument> page = favoriteRepository.findByRenterId(renterId, pageable);
        
        var content = page.getContent().stream()
                .map(favorite -> {
                    PropertyDocument property = propertyRepository.findById(favorite.getPropertyId())
                            .orElseThrow(() -> new NotFoundException("Property not found"));
                    return mapToResponse(favorite, property);
                })
                .collect(Collectors.toList());
        
        return PageResponse.<FavoriteResponse>builder()
                .content(content)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .hasNext(!page.isLast())
                .hasPrevious(page.getNumber() > 0)
                .build();
    }
    
    @Override
    public boolean isFavorite(String propertyId, String renterId) {
        return favoriteRepository.existsByRenterIdAndPropertyId(renterId, propertyId);
    }
    
    @Override
    public long getFavoriteCount(String renterId) {
        return favoriteRepository.countByRenterId(renterId);
    }
    
    @Override
    public FavoriteResponse updateFavoriteNotes(String propertyId, String notes, String renterId) {
        log.info("Updating notes for favorite property {} for renter {}", propertyId, renterId);
        
        FavoriteDocument favorite = favoriteRepository.findByRenterIdAndPropertyId(renterId, propertyId)
                .orElseThrow(() -> new NotFoundException("Property not in favorites"));
        
        favorite.setNotes(notes);
        FavoriteDocument updated = favoriteRepository.save(favorite);
        
        PropertyDocument property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new NotFoundException("Property not found"));
        
        return mapToResponse(updated, property);
    }
    
    private FavoriteResponse mapToResponse(FavoriteDocument favorite, PropertyDocument property) {
        return FavoriteResponse.builder()
                .id(favorite.getId())
                .renterId(favorite.getRenterId())
                .property(propertyMapper.toResponse(property))
                .notes(favorite.getNotes())
                .createdAt(favorite.getCreatedAt())
                .build();
    }
}
