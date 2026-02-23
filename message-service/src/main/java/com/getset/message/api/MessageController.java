package com.getset.message.api;

import com.getset.common.dto.PageResponse;
import com.getset.common.dto.UserSummaryDto;
import com.getset.common.event.NotificationEvent;
import com.getset.common.exception.NotFoundException;
import com.getset.message.client.UserServiceClient;
import com.getset.message.domain.MessageDocument;
import com.getset.message.domain.MessageRepository;
import com.getset.message.event.NotificationEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageRepository messageRepository;
    private final UserServiceClient userServiceClient;
    private final NotificationEventPublisher eventPublisher;

    @PostMapping
    public ResponseEntity<MessageDocument> send(
            @RequestBody Map<String, String> body,
            Principal principal) {

        String senderId = principal.getName();
        String recipientId = body.get("recipientId");
        String content = body.get("content");
        String threadId = body.getOrDefault("threadId", UUID.randomUUID().toString());

        UserSummaryDto sender = userServiceClient.getUserById(senderId);
        UserSummaryDto recipient = userServiceClient.getUserById(recipientId);

        MessageDocument msg = MessageDocument.builder()
                .threadId(threadId)
                .senderId(sender.getId()).senderName(sender.getName()).senderEmail(sender.getEmail())
                .recipientId(recipient.getId()).recipientName(recipient.getName()).recipientEmail(recipient.getEmail())
                .propertyId(body.get("propertyId")).enquiryId(body.get("enquiryId"))
                .content(content).read(false)
                .build();
        MessageDocument saved = messageRepository.save(msg);

        // Notify recipient asynchronously via Kafka
        eventPublisher.publish(NotificationEvent.builder()
                .eventType(NotificationEvent.EventType.MESSAGE_RECEIVED)
                .recipientId(recipient.getId()).recipientEmail(recipient.getEmail()).recipientName(recipient.getName())
                .actorName(sender.getName())
                .extraPayload(content.length() > 100 ? content.substring(0, 97) + "..." : content)
                .relatedEntityId(saved.getId())
                .build());

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/thread/{threadId}")
    public ResponseEntity<PageResponse<MessageDocument>> getThread(@PathVariable String threadId, Pageable pageable) {
        Page<MessageDocument> page = messageRepository.findByThreadIdOrderByCreatedAtAsc(threadId, pageable);
        return ResponseEntity.ok(toPageResponse(page));
    }

    @GetMapping("/received")
    public ResponseEntity<PageResponse<MessageDocument>> getReceived(Principal principal, Pageable pageable) {
        return ResponseEntity.ok(toPageResponse(messageRepository.findByRecipientEmail(principal.getName(), pageable)));
    }

    @GetMapping("/sent")
    public ResponseEntity<PageResponse<MessageDocument>> getSent(Principal principal, Pageable pageable) {
        return ResponseEntity.ok(toPageResponse(messageRepository.findBySenderEmail(principal.getName(), pageable)));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(Principal principal) {
        return ResponseEntity
                .ok(Map.of("count", messageRepository.countByRecipientEmailAndReadFalse(principal.getName())));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<MessageDocument> markRead(@PathVariable String id, Principal principal) {
        MessageDocument msg = messageRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Message not found: " + id));
        if (msg.getRecipientEmail().equals(principal.getName())) {
            msg.setRead(true);
            messageRepository.save(msg);
        }
        return ResponseEntity.ok(msg);
    }

    private <T> PageResponse<T> toPageResponse(Page<T> page) {
        return PageResponse.<T>builder()
                .content(page.getContent()).pageNumber(page.getNumber()).pageSize(page.getSize())
                .totalElements(page.getTotalElements()).totalPages(page.getTotalPages())
                .hasNext(!page.isLast()).hasPrevious(page.getNumber() > 0).build();
    }
}
