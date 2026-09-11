package com.getset.favorite.domain;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "favorites")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteDocument {
    @Id
    private String id;
    private String renterId;
    private String propertyId;
    private String propertyTitle;
    private String propertyCity;
    private double pricePerMonth;
    private String notes;
    @CreatedDate
    private Instant createdAt;
}
