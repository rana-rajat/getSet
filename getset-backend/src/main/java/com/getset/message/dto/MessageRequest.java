package com.getset.message.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageRequest {
    
    @NotBlank(message = "Recipient ID is required")
    private String recipientId;
    
    private String propertyId;
    private String enquiryId;
    
    @NotBlank(message = "Message content is required")
    private String content;
    
    private String threadId; // For replies in existing conversation
}
