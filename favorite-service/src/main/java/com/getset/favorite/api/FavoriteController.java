package com.getset.favorite.api;

import com.getset.common.dto.PageResponse;
import com.getset.common.dto.PropertySummaryDto;
import com.getset.common.exception.NotFoundException;
import com.getset.favorite.client.PropertyServiceClient;
import com.getset.favorite.domain.FavoriteDocument;
import com.getset.favorite.domain.FavoriteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteRepository favoriteRepository;
    private final PropertyServiceClient propertyServiceClient;

    @PostMapping("/{propertyId}")
    public ResponseEntity<FavoriteDocument> addFavorite(@PathVariable String propertyId,
            @RequestParam(required = false) String notes,
            Principal principal) {
        if (favoriteRepository.existsByRenterIdAndPropertyId(principal.getName(), propertyId)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        PropertySummaryDto property = propertyServiceClient.getPropertyById(propertyId);
        FavoriteDocument fav = FavoriteDocument.builder()
                .renterId(principal.getName())
                .propertyId(property.getId()).propertyTitle(property.getTitle())
                .propertyCity(property.getCity()).pricePerMonth(property.getPricePerMonth())
                .notes(notes)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(favoriteRepository.save(fav));
    }

    @DeleteMapping("/{propertyId}")
    public ResponseEntity<Map<String, String>> removeFavorite(@PathVariable String propertyId, Principal principal) {
        favoriteRepository.deleteByRenterIdAndPropertyId(principal.getName(), propertyId);
        return ResponseEntity.ok(Map.of("message", "Removed from favorites"));
    }

    @GetMapping
    public ResponseEntity<PageResponse<FavoriteDocument>> getMyFavorites(Principal principal, Pageable pageable) {
        Page<FavoriteDocument> page = favoriteRepository.findByRenterId(principal.getName(), pageable);
        return ResponseEntity.ok(PageResponse.<FavoriteDocument>builder()
                .content(page.getContent()).pageNumber(page.getNumber()).pageSize(page.getSize())
                .totalElements(page.getTotalElements()).totalPages(page.getTotalPages())
                .hasNext(!page.isLast()).hasPrevious(page.getNumber() > 0).build());
    }

    @GetMapping("/check/{propertyId}")
    public ResponseEntity<Map<String, Boolean>> isFavorite(@PathVariable String propertyId, Principal principal) {
        boolean isFav = favoriteRepository.existsByRenterIdAndPropertyId(principal.getName(), propertyId);
        return ResponseEntity.ok(Map.of("isFavorite", isFav));
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getCount(Principal principal) {
        return ResponseEntity.ok(Map.of("count", favoriteRepository.countByRenterId(principal.getName())));
    }
}
