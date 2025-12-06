package com.getset.enquiry;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "enquiries")
public class EnquiryDocument {
    
    @Id
    private String id;
    
    private String propertyId;
    private String renterId;
    private String ownerId;
    
    private String message;
    
    @Builder.Default
    private EnquiryStatus status = EnquiryStatus.PENDING;
    
    private String rejectionReason;
    
    @CreatedDate
    private Instant createdAt;
    
    @LastModifiedDate
    private Instant updatedAt;
}
