package com.getset.favorite;

import com.getset.common.PageResponse;
import com.getset.favorite.dto.FavoriteRequest;
import com.getset.favorite.dto.FavoriteResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "Favorites", description = "Property favorites management endpoints")
@RequestMapping("/api/v1/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    /**
     * Add property to favorites
     */
    @PostMapping
    @PreAuthorize("hasRole('RENTER')")
    @Operation(summary = "Add property to favorites")
    public ResponseEntity<FavoriteResponse> addFavorite(
            @Valid @RequestBody FavoriteRequest request,
            @RequestHeader("X-User-Id") String renterId) {
        FavoriteResponse response = favoriteService.addFavorite(request, renterId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Remove property from favorites
     */
    @DeleteMapping("/{propertyId}")
    @PreAuthorize("hasRole('RENTER')")
    @Operation(summary = "Remove property from favorites")
    public ResponseEntity<Void> removeFavorite(
            @PathVariable String propertyId,
            @RequestHeader("X-User-Id") String renterId) {
        favoriteService.removeFavorite(propertyId, renterId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get all favorites
     */
    @GetMapping
    @PreAuthorize("hasRole('RENTER')")
    @Operation(summary = "Get all favorites")
    public ResponseEntity<PageResponse<FavoriteResponse>> getFavorites(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader("X-User-Id") String renterId) {
        Pageable pageable = PageRequest.of(page, size);
        PageResponse<FavoriteResponse> response = favoriteService.getFavorites(renterId, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Check if property is in favorites
     */
    @GetMapping("/check/{propertyId}")
    @PreAuthorize("hasRole('RENTER')")
    @Operation(summary = "Check if property is in favorites")
    public ResponseEntity<?> isFavorite(
            @PathVariable String propertyId,
            @RequestHeader("X-User-Id") String renterId) {
        boolean isFavorite = favoriteService.isFavorite(propertyId, renterId);
        return ResponseEntity.ok(java.util.Map.of("isFavorite", isFavorite));
    }

    /**
     * Get favorite count
     */
    @GetMapping("/count")
    @PreAuthorize("hasRole('RENTER')")
    @Operation(summary = "Get favorite count")
    public ResponseEntity<?> getFavoriteCount(
            @RequestHeader("X-User-Id") String renterId) {
        long count = favoriteService.getFavoriteCount(renterId);
        return ResponseEntity.ok(java.util.Map.of("count", count));
    }

    /**
     * Update favorite notes
     */
    @PutMapping("/{propertyId}")
    @PreAuthorize("hasRole('RENTER')")
    @Operation(summary = "Update favorite notes")
    public ResponseEntity<FavoriteResponse> updateFavoriteNotes(
            @PathVariable String propertyId,
            @RequestParam String notes,
            @RequestHeader("X-User-Id") String renterId) {
        FavoriteResponse response = favoriteService.updateFavoriteNotes(propertyId, notes, renterId);
        return ResponseEntity.ok(response);
    }
}
