package com.getset.enquiry.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface EnquiryRepository extends MongoRepository<EnquiryDocument, String> {
    Page<EnquiryDocument> findByPropertyId(String propertyId, Pageable pageable);

    Page<EnquiryDocument> findByRenterId(String renterId, Pageable pageable);

    Page<EnquiryDocument> findByOwnerId(String ownerId, Pageable pageable);

    Page<EnquiryDocument> findByRenterEmail(String renterEmail, Pageable pageable);

    Page<EnquiryDocument> findByOwnerEmail(String ownerEmail, Pageable pageable);
}
