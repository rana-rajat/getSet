package com.getset.message;

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
@Document(collection = "messages")
public class MessageDocument {
    
    @Id
    private String id;
    
    private String senderId;
    private String senderName;
    private String senderEmail;
    private String recipientId;
    private String recipientName;
    private String recipientEmail;
    
    private String propertyId;
    private String enquiryId; // Link to parent enquiry
    
    private String content;
    
    @Builder.Default
    private boolean read = false;
    
    @CreatedDate
    private Instant createdAt;
    
    // Conversation thread ID - all messages in a conversation share the same threadId
    private String threadId;
}
