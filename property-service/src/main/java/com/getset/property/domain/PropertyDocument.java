package com.getset.property.domain;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;

import java.time.Instant;

@Document(collection = "properties")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyDocument {
    @Id
    private String id;
    private String title;
    private String description;
    private double pricePerMonth;
    private int bedrooms;
    private int bathrooms;
    private double areaSqFt;
    private boolean furnished;
    private boolean available;
    private String propertyType; // APARTMENT, HOUSE, PG, VILLA
    private String ownerId;
    private String city;
    private String state;
    private String fullAddress;
    private String pincode;
    @GeoSpatialIndexed
    private GeoJsonPoint location;
    private java.util.List<String> amenities;
    private java.util.List<String> imageUrls;
    @CreatedDate
    private Instant createdAt;
    @LastModifiedDate
    private Instant updatedAt;
}
