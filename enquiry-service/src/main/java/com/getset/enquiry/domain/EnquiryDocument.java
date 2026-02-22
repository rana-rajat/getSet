package com.getset.enquiry.domain;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "enquiries")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnquiryDocument {
    @Id
    private String id;
    private String propertyId;
    private String propertyTitle;
    private String ownerId;
    private String ownerName;
    private String ownerEmail;
    private String renterId;
    private String renterName;
    private String renterEmail;
    private String message;
    private EnquiryStatus status;
    private String rejectionReason;
    @CreatedDate
    private Instant createdAt;
    @LastModifiedDate
    private Instant updatedAt;
}
