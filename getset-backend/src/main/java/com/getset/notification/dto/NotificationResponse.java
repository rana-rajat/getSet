package com.getset.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    
    private String id;
    private String subject;
    private String body;
    private String type;
    private String relatedEntityId;
    private boolean read;
    private boolean emailSent;
    private Instant createdAt;
}
