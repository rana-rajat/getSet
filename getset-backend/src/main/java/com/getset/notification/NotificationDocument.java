package com.getset.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "notifications")
public class NotificationDocument {
    
    @Id
    private String id;
    
    private String recipientId;
    private String recipientEmail;
    private String subject;
    private String body;
    private String type; // ENQUIRY_RECEIVED, ENQUIRY_ACCEPTED, ENQUIRY_REJECTED, MESSAGE_RECEIVED, FAVORITE_ADDED
    private String relatedEntityId; // enquiry ID, message ID, property ID, etc.
    
    @Builder.Default
    private boolean read = false;
    
    @Builder.Default
    private boolean emailSent = false;
    
    private String emailSentError;
    
    @CreatedDate
    private Instant createdAt;
}
