package com.getset.property.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface PropertyRepository extends MongoRepository<PropertyDocument, String> {
    Optional<PropertyDocument> findByIdAndOwnerId(String id, String ownerId);

    Page<PropertyDocument> findByOwnerId(String ownerId, Pageable pageable);

    Page<PropertyDocument> findByCity(String city, Pageable pageable);
}
