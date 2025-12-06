package com.getset.property;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Location {
    private String type;  // "Point"
    private double[] coordinates;  // [lng, lat]

    public static Location fromCoordinates(double longitude, double latitude) {
        return Location.builder()
                .type("Point")
                .coordinates(new double[]{longitude, latitude})
                .build();
    }

    public double getLongitude() {
        return coordinates != null && coordinates.length > 0 ? coordinates[0] : 0;
    }

    public double getLatitude() {
        return coordinates != null && coordinates.length > 1 ? coordinates[1] : 0;
    }
}
