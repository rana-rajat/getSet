package com.getset.message.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MessageRepository extends MongoRepository<MessageDocument, String> {
    Page<MessageDocument> findByThreadIdOrderByCreatedAtAsc(String threadId, Pageable pageable);

    List<MessageDocument> findByRecipientIdAndReadFalse(String recipientId);

    long countByRecipientIdAndReadFalse(String recipientId);

    Page<MessageDocument> findBySenderId(String senderId, Pageable pageable);

    Page<MessageDocument> findByRecipientId(String recipientId, Pageable pageable);
}
