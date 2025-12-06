package com.getset.favorite;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FavoriteRepository extends MongoRepository<FavoriteDocument, String> {
    
    Page<FavoriteDocument> findByRenterId(String renterId, Pageable pageable);
    
    Optional<FavoriteDocument> findByRenterIdAndPropertyId(String renterId, String propertyId);
    
    boolean existsByRenterIdAndPropertyId(String renterId, String propertyId);
    
    long countByRenterId(String renterId);
    
    void deleteByRenterIdAndPropertyId(String renterId, String propertyId);
}
