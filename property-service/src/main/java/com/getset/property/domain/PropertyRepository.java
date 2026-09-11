package com.getset.property.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;

public interface PropertyRepository extends MongoRepository<PropertyDocument, String> {
    Optional<PropertyDocument> findByIdAndOwnerId(String id, String ownerId);

    Page<PropertyDocument> findByOwnerId(String ownerId, Pageable pageable);

    Page<PropertyDocument> findByCityIgnoreCase(String city, Pageable pageable);

    Page<PropertyDocument> findByPropertyTypeIgnoreCase(String propertyType, Pageable pageable);

    Page<PropertyDocument> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    Page<PropertyDocument> findByPricePerMonthBetween(double min, double max, Pageable pageable);

    Page<PropertyDocument> findByBedroomsGreaterThanEqual(int bedrooms, Pageable pageable);

    /**
     * Full flexible search: all optional params. Null-safe via $ne trick skipped;
     * we use a custom @Query supporting city, type, minPrice, maxPrice, bedrooms, keyword.
     */
    @Query("{ $and: [ " +
           "  { $or: [ { ?0: null }, { 'city': { $regex: ?0, $options: 'i' } } ] }, " +
           "  { $or: [ { ?1: null }, { 'propertyType': { $regex: ?1, $options: 'i' } } ] }, " +
           "  { 'pricePerMonth': { $gte: ?2, $lte: ?3 } }, " +
           "  { $or: [ { ?4: 0 }, { 'bedrooms': { $gte: ?4 } } ] }, " +
           "  { $or: [ { ?5: null }, { 'title': { $regex: ?5, $options: 'i' } } ] } " +
           "] }")
    Page<PropertyDocument> searchWithFilters(
            String city,
            String propertyType,
            double minPrice,
            double maxPrice,
            int bedrooms,
            String keyword,
            Pageable pageable
    );
}
