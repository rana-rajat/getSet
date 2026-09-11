package com.getset.property.api;

import com.getset.common.dto.PropertySummaryDto;
import com.getset.common.exception.NotFoundException;
import com.getset.property.domain.PropertyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * Internal endpoint called by enquiry-service and favorite-service via Feign.
 * NOT routed through the API Gateway to external clients.
 */
@RestController
@RequestMapping("/internal/properties")
@RequiredArgsConstructor
public class InternalPropertyController {

    private final PropertyRepository propertyRepository;

    @GetMapping("/{id}")
    public PropertySummaryDto getPropertyById(@PathVariable String id) {
        return propertyRepository.findById(id)
                .map(p -> PropertySummaryDto.builder()
                        .id(p.getId()).title(p.getTitle())
                        .ownerId(p.getOwnerId()).city(p.getCity())
                        .pricePerMonth(p.getPricePerMonth()).build())
                .orElseThrow(() -> new NotFoundException("Property not found: " + id));
    }
}
