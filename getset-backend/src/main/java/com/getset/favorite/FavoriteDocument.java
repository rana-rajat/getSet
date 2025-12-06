package com.getset.favorite;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "favorites")
public class FavoriteDocument {
    
    @Id
    private String id;
    
    private String renterId;
    private String propertyId;
    
    private String notes;
    
    @CreatedDate
    private Instant createdAt;
}
