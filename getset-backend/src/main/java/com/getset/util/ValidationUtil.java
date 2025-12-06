package com.getset.util;

/**
 * Validation utilities for common validation operations.
 */
public class ValidationUtil {
    
    private ValidationUtil() {
        throw new AssertionError("Cannot instantiate utility class");
    }
    
    /**
     * Validates latitude value.
     * @param lat latitude value
     * @return true if valid, false otherwise
     */
    public static boolean isValidLatitude(double lat) {
        return lat >= -90.0 && lat <= 90.0;
    }
    
    /**
     * Validates longitude value.
     * @param lng longitude value
     * @return true if valid, false otherwise
     */
    public static boolean isValidLongitude(double lng) {
        return lng >= -180.0 && lng <= 180.0;
    }
    
    /**
     * Validates email format.
     * @param email email address
     * @return true if valid email format, false otherwise
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }
    
    /**
     * Validates password strength.
     * @param password password string
     * @return true if password meets minimum requirements
     */
    public static boolean isStrongPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};:',.<>?/].*");
        
        return hasUpper && hasLower && hasDigit && hasSpecial;
    }
    
    /**
     * Validates that a string is not null or blank.
     * @param value string value
     * @param fieldName field name for error message
     * @throws IllegalArgumentException if value is null or blank
     */
    public static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " cannot be null or blank");
        }
    }
}
