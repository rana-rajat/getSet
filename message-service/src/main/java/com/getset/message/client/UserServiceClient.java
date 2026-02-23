package com.getset.message.client;

import com.getset.common.dto.UserSummaryDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", url = "${services.user-service.url}")
public interface UserServiceClient {
    @GetMapping("/internal/users/{id}")
    UserSummaryDto getUserById(@PathVariable String id);
}
