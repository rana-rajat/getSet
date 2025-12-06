package com.getset.message;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MessageRepository extends MongoRepository<MessageDocument, String> {
    
    Page<MessageDocument> findByRecipientId(String recipientId, Pageable pageable);
    
    Page<MessageDocument> findBySenderId(String senderId, Pageable pageable);
    
    Page<MessageDocument> findByThreadId(String threadId, Pageable pageable);
    
    Page<MessageDocument> findByRecipientIdAndRead(String recipientId, boolean read, Pageable pageable);
    
    List<MessageDocument> findByThreadIdOrderByCreatedAtDesc(String threadId);
    
    long countByRecipientIdAndRead(String recipientId, boolean read);
    
    Optional<MessageDocument> findFirstByThreadIdOrderByCreatedAtDesc(String threadId);
    
    List<MessageDocument> findByPropertyIdAndSenderIdAndRecipientId(String propertyId, String senderId, String recipientId);
    
    // Get distinct conversation partners for a user
    @Query("{ $or: [{ 'senderId': ?0 }, { 'recipientId': ?0 }] }")
    Page<MessageDocument> findUserConversations(String userId, Pageable pageable);
}
