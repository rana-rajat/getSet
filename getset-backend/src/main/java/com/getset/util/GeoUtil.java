package com.getset.util;

/**
 * Geospatial utilities for location-based operations.
 */
public class GeoUtil {
    
    private GeoUtil() {
        throw new AssertionError("Cannot instantiate utility class");
    }
    
    private static final double EARTH_RADIUS_KM = 6371.0;
    
    /**
     * Calculates distance between two coordinates using Haversine formula.
     * @param lat1 latitude of first point
     * @param lng1 longitude of first point
     * @param lat2 latitude of second point
     * @param lng2 longitude of second point
     * @return distance in kilometers
     */
    public static double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }
    
    /**
     * Converts kilometers to meters.
     * @param km distance in kilometers
     * @return distance in meters
     */
    public static double kmToMeters(double km) {
        return km * 1000;
    }
    
    /**
     * Converts meters to kilometers.
     * @param meters distance in meters
     * @return distance in kilometers
     */
    public static double metersToKm(double meters) {
        return meters / 1000.0;
    }
}
