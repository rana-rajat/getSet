package com.getset.enquiry;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnquiryRepository extends MongoRepository<EnquiryDocument, String> {
    
    Page<EnquiryDocument> findByRenterId(String renterId, Pageable pageable);
    
    Page<EnquiryDocument> findByOwnerId(String ownerId, Pageable pageable);
    
    Page<EnquiryDocument> findByPropertyId(String propertyId, Pageable pageable);
    
    List<EnquiryDocument> findByPropertyIdAndStatus(String propertyId, EnquiryStatus status);
    
    long countByOwnerId(String ownerId);
    
    long countByOwnerIdAndStatus(String ownerId, EnquiryStatus status);
    
    long countByPropertyId(String propertyId);
    
    Optional<EnquiryDocument> findByIdAndOwnerId(String id, String ownerId);
    
    Optional<EnquiryDocument> findByIdAndRenterId(String id, String renterId);
}
