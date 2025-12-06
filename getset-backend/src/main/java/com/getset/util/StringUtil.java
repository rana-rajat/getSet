package com.getset.util;

/**
 * String utilities for common string operations.
 */
public class StringUtil {
    
    private StringUtil() {
        throw new AssertionError("Cannot instantiate utility class");
    }
    
    /**
     * Checks if string is null or empty.
     * @param str string to check
     * @return true if null or empty, false otherwise
     */
    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }
    
    /**
     * Checks if string is null or blank (whitespace).
     * @param str string to check
     * @return true if null or blank, false otherwise
     */
    public static boolean isBlank(String str) {
        return str == null || str.isBlank();
    }
    
    /**
     * Safely converts object to string.
     * @param obj object to convert
     * @return string representation or "null" if object is null
     */
    public static String toString(Object obj) {
        return obj == null ? "null" : obj.toString();
    }
    
    /**
     * Capitalizes first letter of string.
     * @param str string to capitalize
     * @return capitalized string
     */
    public static String capitalize(String str) {
        if (isEmpty(str)) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
    
    /**
     * Truncates string to specified length.
     * @param str string to truncate
     * @param maxLength maximum length
     * @return truncated string
     */
    public static String truncate(String str, int maxLength) {
        if (isEmpty(str) || str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength) + "...";
    }
}
