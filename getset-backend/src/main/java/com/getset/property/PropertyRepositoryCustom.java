package com.getset.property;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PropertyRepositoryCustom {
    Page<PropertyDocument> searchProperties(
            String city,
            Double minPrice,
            Double maxPrice,
            Integer minBedrooms,
            Boolean furnished,
            PropertyType type,
            Pageable pageable);
}
