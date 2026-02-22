package com.getset.favorite.client;

import com.getset.common.dto.PropertySummaryDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "property-service", url = "${services.property-service.url}")
public interface PropertyServiceClient {
    @GetMapping("/internal/properties/{id}")
    PropertySummaryDto getPropertyById(@PathVariable String id);
}
