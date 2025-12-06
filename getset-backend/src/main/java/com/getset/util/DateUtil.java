package com.getset.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Date and time utilities.
 */
public class DateUtil {
    
    private DateUtil() {
        throw new AssertionError("Cannot instantiate utility class");
    }
    
    private static final DateTimeFormatter STANDARD_FORMAT = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Converts Instant to formatted string.
     * @param instant instant to format
     * @return formatted date string
     */
    public static String format(Instant instant) {
        if (instant == null) {
            return null;
        }
        LocalDateTime ldt = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        return ldt.format(STANDARD_FORMAT);
    }
    
    /**
     * Gets current instant.
     * @return current instant
     */
    public static Instant now() {
        return Instant.now();
    }
    
    /**
     * Checks if an instant is in the past.
     * @param instant instant to check
     * @return true if past, false otherwise
     */
    public static boolean isPast(Instant instant) {
        return instant != null && instant.isBefore(Instant.now());
    }
    
    /**
     * Checks if an instant is in the future.
     * @param instant instant to check
     * @return true if future, false otherwise
     */
    public static boolean isFuture(Instant instant) {
        return instant != null && instant.isAfter(Instant.now());
    }
}
