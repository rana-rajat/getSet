package com.getset.constant;

/**
 * Application-wide constants.
 */
public class AppConstants {
    
    private AppConstants() {
        throw new AssertionError("Cannot instantiate constants class");
    }
    
    // API Versions
    public static final String API_VERSION = "v1";
    public static final String API_BASE_PATH = "/api/" + API_VERSION;
    
    // Pagination
    public static final int DEFAULT_PAGE_NUMBER = 0;
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int MAX_PAGE_SIZE = 100;
    
    // Geospatial
    public static final double DEFAULT_SEARCH_RADIUS_KM = 5.0;
    public static final double MAX_SEARCH_RADIUS_KM = 50.0;
    public static final int GEOSPATIAL_INDEX_DISTANCE = 2;
    
    // JWT
    public static final String JWT_HEADER = "Authorization";
    public static final String JWT_PREFIX = "Bearer ";
    public static final long JWT_EXPIRATION_MS = 86400000; // 24 hours
    
    // Cache
    public static final String CACHE_PROPERTIES = "properties";
    public static final String CACHE_USERS = "users";
    
    // Error Messages
    public static final String PROPERTY_NOT_FOUND = "Property not found";
    public static final String USER_NOT_FOUND = "User not found";
    public static final String UNAUTHORIZED_ACCESS = "You are not authorized to access this resource";
    public static final String FORBIDDEN_OPERATION = "You don't have permission to perform this operation";
    public static final String INVALID_INPUT = "Invalid input provided";
}
