package com.getset.search.controller;

import com.getset.search.dto.SearchRequestDto;
import com.getset.search.dto.SearchResponseDto;
import com.getset.search.service.PropertySearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Search Controller
 * Provides REST endpoints for property search and discovery
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
@Tag(name = "Search & Discovery", description = "Property search and discovery endpoints")
public class SearchController {

    private final PropertySearchService propertySearchService;

    /**
     * Full-text search with filters
     * GET /api/v1/search/full-text?text=apartment&city=Mumbai&minPrice=30000&maxPrice=50000
     */
    @GetMapping("/full-text")
    @Operation(summary = "Full-text property search with filters")
    public ResponseEntity<SearchResponseDto> fullTextSearch(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Integer minBedrooms,
            @RequestParam(required = false) Boolean furnished,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "false") Boolean ascending) {

        log.info("Full-text search - text: {}, city: {}, minPrice: {}", text, city, minPrice);

        SearchRequestDto request = SearchRequestDto.builder()
                .text(text)
                .city(city)
                .type(type)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .minBedrooms(minBedrooms)
                .furnished(furnished)
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .ascending(ascending)
                .build();

        SearchResponseDto response = propertySearchService.search(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get autocomplete suggestions
     * GET /api/v1/search/suggestions?q=apartment&limit=5
     */
    @GetMapping("/suggestions")
    @Operation(summary = "Get search suggestions")
    public ResponseEntity<List<String>> getSuggestions(
            @RequestParam(name = "q") String query,
            @RequestParam(defaultValue = "5") Integer limit) {

        log.info("Getting suggestions for query: {}", query);

        List<String> suggestions = propertySearchService.getSuggestions(query, limit);
        return ResponseEntity.ok(suggestions);
    }

    /**
     * Simple search convenience endpoint
     * GET /api/v1/search?city=Mumbai&minPrice=30000&maxPrice=50000
     */
    @GetMapping
    @Operation(summary = "Search properties with basic filters")
    public ResponseEntity<SearchResponseDto> search(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Integer minBedrooms,
            @RequestParam(required = false) Boolean furnished,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        log.info("Basic search - city: {}, minPrice: {}", city, minPrice);

        SearchRequestDto request = SearchRequestDto.builder()
                .city(city)
                .type(type)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .minBedrooms(minBedrooms)
                .furnished(furnished)
                .page(page)
                .size(size)
                .sortBy("createdAt")
                .ascending(false)
                .build();

        SearchResponseDto response = propertySearchService.search(request);
        return ResponseEntity.ok(response);
    }
}
