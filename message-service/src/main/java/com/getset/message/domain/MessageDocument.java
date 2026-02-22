package com.getset.message.domain;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "messages")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageDocument {
    @Id
    private String id;
    private String threadId;
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
    @CreatedDate
    private Instant createdAt;
}
