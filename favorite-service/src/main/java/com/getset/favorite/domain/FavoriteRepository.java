package com.getset.favorite.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface FavoriteRepository extends MongoRepository<FavoriteDocument, String> {
    Page<FavoriteDocument> findByRenterId(String renterId, Pageable pageable);

    Optional<FavoriteDocument> findByRenterIdAndPropertyId(String renterId, String propertyId);

    boolean existsByRenterIdAndPropertyId(String renterId, String propertyId);

    void deleteByRenterIdAndPropertyId(String renterId, String propertyId);

    long countByRenterId(String renterId);
}
