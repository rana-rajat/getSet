package com.getset.property;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PropertyRepository extends MongoRepository<PropertyDocument, String>, PropertyRepositoryCustom {
    
    List<PropertyDocument> findByAddress_CityAndIsActive(String city, boolean isActive);
    
    List<PropertyDocument> findByOwnerIdAndIsActive(String ownerId, boolean isActive);
    
    Optional<PropertyDocument> findByIdAndOwnerId(String id, String ownerId);

    @Query("{ 'location': { $nearSphere: { $geometry: { type: 'Point', coordinates: [?0, ?1] }, $maxDistance: ?2 } }, 'isActive': true }")
    List<PropertyDocument> findNearby(double lng, double lat, double maxDistanceMeters);
}
