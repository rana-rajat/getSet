package com.getset.message.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {
    
    private String id;
    private String senderId;
    private String senderName;
    private String senderEmail;
    private String recipientId;
    private String recipientName;
    private String recipientEmail;
    private String propertyId;
    private String enquiryId;
    private String content;
    private boolean read;
    private String threadId;
    private Instant createdAt;
}
