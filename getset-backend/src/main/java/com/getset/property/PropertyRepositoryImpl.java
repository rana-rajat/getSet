package com.getset.property;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Repository
public class PropertyRepositoryImpl implements PropertyRepositoryCustom {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public Page<PropertyDocument> searchProperties(
            String city,
            Double minPrice,
            Double maxPrice,
            Integer minBedrooms,
            Boolean furnished,
            PropertyType type,
            Pageable pageable) {

        Query query = new Query();
        Criteria criteria = Criteria.where("isActive").is(true);

        if (city != null && !city.isEmpty()) {
            criteria = criteria.and("address.city").is(city);
        }
        if (minPrice != null) {
            criteria = criteria.and("pricePerMonth").gte(minPrice);
        }
        if (maxPrice != null) {
            criteria = criteria.and("pricePerMonth").lte(maxPrice);
        }
        if (minBedrooms != null) {
            criteria = criteria.and("bedrooms").gte(minBedrooms);
        }
        if (furnished != null) {
            criteria = criteria.and("furnished").is(furnished);
        }
        if (type != null) {
            criteria = criteria.and("type").is(type);
        }

        query.addCriteria(criteria);

        long total = mongoTemplate.count(query, PropertyDocument.class);

        query.with(pageable);
        List<PropertyDocument> properties = mongoTemplate.find(query, PropertyDocument.class);

        return new PageImpl<>(properties, pageable, total);
    }
}
