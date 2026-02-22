package com.getset.search.service;

import com.getset.search.dto.SearchRequestDto;
import com.getset.search.dto.SearchResponseDto;

/**
 * Property Search Service
 * Defines contract for full-text property search using Elasticsearch
 */
public interface PropertySearchService {

    /**
     * Search properties with full-text and filtered search
     *
     * @param request Search request with query and filters
     * @return Paginated search results
     */
    SearchResponseDto search(SearchRequestDto request);

    /**
     * Get autocomplete suggestions for search query
     *
     * @param query Partial search text
     * @param limit Number of suggestions
     * @return List of suggestions
     */
    java.util.List<String> getSuggestions(String query, Integer limit);
}
