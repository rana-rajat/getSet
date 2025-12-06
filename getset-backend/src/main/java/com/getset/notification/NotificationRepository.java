package com.getset.notification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends MongoRepository<NotificationDocument, String> {
    
    Page<NotificationDocument> findByRecipientId(String recipientId, Pageable pageable);
    
    Page<NotificationDocument> findByRecipientIdAndRead(String recipientId, boolean read, Pageable pageable);
    
    long countByRecipientIdAndRead(String recipientId, boolean read);
    
    List<NotificationDocument> findByEmailSentFalse();
}
