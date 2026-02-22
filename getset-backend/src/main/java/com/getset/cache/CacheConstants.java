package com.getset.cache;

/**
 * Cache Constants
 * Defines cache key prefixes and TTL values for Redis caching strategy
 */
public class CacheConstants {

    // Cache key prefixes
    public static final String PROPERTY_CACHE_PREFIX = "property:";
    public static final String SEARCH_CACHE_PREFIX = "search:";
    public static final String FACETS_CACHE_PREFIX = "facets:";

    // Cache TTL in seconds
    public static final long PROPERTY_CACHE_TTL = 24 * 60 * 60; // 24 hours
    public static final long SEARCH_CACHE_TTL = 60 * 60; // 1 hour
    public static final long FACETS_CACHE_TTL = 6 * 60 * 60; // 6 hours

    private CacheConstants() {
        // Private constructor to prevent instantiation
    }
}
