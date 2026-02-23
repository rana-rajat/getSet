package com.getset.message.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MessageRepository extends MongoRepository<MessageDocument, String> {
    Page<MessageDocument> findByThreadIdOrderByCreatedAtAsc(String threadId, Pageable pageable);

    List<MessageDocument> findByRecipientIdAndReadFalse(String recipientId);

    long countByRecipientIdAndReadFalse(String recipientId);

    long countByRecipientEmailAndReadFalse(String recipientEmail);

    Page<MessageDocument> findBySenderId(String senderId, Pageable pageable);

    Page<MessageDocument> findBySenderEmail(String senderEmail, Pageable pageable);

    Page<MessageDocument> findByRecipientId(String recipientId, Pageable pageable);

    Page<MessageDocument> findByRecipientEmail(String recipientEmail, Pageable pageable);
}
