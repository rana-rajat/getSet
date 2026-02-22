package com.getset.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Lightweight user data returned by user-service's internal endpoint.
 * Used by enquiry-service, message-service, and favorite-service via Feign.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSummaryDto {
    private String id;
    private String name;
    private String email;
    private String role;
    private String phone;
}
