package com.getset.property;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "properties")
public class PropertyDocument {

    @Id
    private String id;

    private String ownerId;

    private String title;
    private String description;
    private PropertyType type;

    private Double pricePerMonth;
    private Integer bedrooms;
    private Integer bathrooms;
    private Boolean furnished;
    private List<String> amenities;

    private Address address;

    private Location location;

    private List<String> photos;

    @Builder.Default
    private Boolean isActive = true;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
