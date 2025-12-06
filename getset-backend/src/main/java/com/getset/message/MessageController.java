package com.getset.message;

import com.getset.common.PageResponse;
import com.getset.message.dto.ConversationResponse;
import com.getset.message.dto.MessageRequest;
import com.getset.message.dto.MessageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
public class MessageController {
    
    private final MessageService messageService;
    
    /**
     * Send a message
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MessageResponse> sendMessage(
            @Valid @RequestBody MessageRequest request,
            @RequestHeader("X-User-Id") String senderId,
            @RequestHeader("X-User-Name") String senderName,
            @RequestHeader("X-User-Email") String senderEmail) {
        MessageResponse response = messageService.sendMessage(request, senderId, senderName, senderEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Get messages in a conversation thread
     */
    @GetMapping("/thread/{threadId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<MessageResponse>> getConversation(
            @PathVariable String threadId,
            @RequestHeader("X-User-Id") String userId) {
        List<MessageResponse> messages = messageService.getConversation(threadId, userId);
        return ResponseEntity.ok(messages);
    }
    
    /**
     * Get received messages
     */
    @GetMapping("/received")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PageResponse<MessageResponse>> getReceivedMessages(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader("X-User-Id") String userId) {
        Pageable pageable = PageRequest.of(page, size);
        PageResponse<MessageResponse> response = messageService.getReceivedMessages(userId, pageable);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get sent messages
     */
    @GetMapping("/sent")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PageResponse<MessageResponse>> getSentMessages(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader("X-User-Id") String userId) {
        Pageable pageable = PageRequest.of(page, size);
        PageResponse<MessageResponse> response = messageService.getSentMessages(userId, pageable);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get unread messages
     */
    @GetMapping("/unread")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PageResponse<MessageResponse>> getUnreadMessages(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader("X-User-Id") String userId) {
        Pageable pageable = PageRequest.of(page, size);
        PageResponse<MessageResponse> response = messageService.getUnreadMessages(userId, pageable);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get unread count
     */
    @GetMapping("/unread/count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getUnreadCount(
            @RequestHeader("X-User-Id") String userId) {
        long count = messageService.getUnreadCount(userId);
        return ResponseEntity.ok(java.util.Map.of("unreadCount", count));
    }
    
    /**
     * Mark message as read
     */
    @PutMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> markMessageAsRead(@PathVariable String id) {
        messageService.markMessageAsRead(id);
        return ResponseEntity.ok().build();
    }
    
    /**
     * Mark all messages as read
     */
    @PutMapping("/read-all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> markAllAsRead(
            @RequestHeader("X-User-Id") String userId) {
        messageService.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * Get conversation with specific user for property
     */
    @GetMapping("/conversation/{otherUserId}/{propertyId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ConversationResponse> getConversationWithUser(
            @PathVariable String otherUserId,
            @PathVariable String propertyId,
            @RequestHeader("X-User-Id") String userId) {
        ConversationResponse response = messageService.getConversationWithUser(userId, otherUserId, propertyId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Delete message
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteMessage(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId) {
        messageService.deleteMessage(id, userId);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Get all conversations for a user
     */
    @GetMapping("/conversations")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PageResponse<ConversationResponse>> getUserConversations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader("X-User-Id") String userId) {
        Pageable pageable = PageRequest.of(page, size);
        PageResponse<ConversationResponse> response = messageService.getUserConversations(userId, pageable);
        return ResponseEntity.ok(response);
    }
}
