package com.getset.property;

import java.util.List;

public interface PropertyRepositoryCustom {
    List<PropertyDocument> searchProperties(
            String city,
            Double minPrice,
            Double maxPrice,
            Integer minBedrooms,
            Boolean furnished,
            PropertyType type,
            int page,
            int size
    );
}
