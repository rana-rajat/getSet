package com.getset.message;

import com.getset.common.NotFoundException;
import com.getset.common.PageResponse;
import com.getset.message.dto.ConversationResponse;
import com.getset.message.dto.MessageRequest;
import com.getset.message.dto.MessageResponse;
import com.getset.notification.NotificationService;
import com.getset.user.UserDocument;
import com.getset.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {
    
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    
    @Override
    public MessageResponse sendMessage(MessageRequest request, String senderId, String senderName, String senderEmail) {
        log.info("Sending message from {} to {}", senderId, request.getRecipientId());
        
        // Verify recipient exists
        UserDocument recipient = userRepository.findById(request.getRecipientId())
                .orElseThrow(() -> new NotFoundException("Recipient not found"));
        
        // Generate thread ID if not provided (new conversation)
        String threadId = request.getThreadId() != null ? 
                request.getThreadId() : 
                generateThreadId(senderId, request.getRecipientId(), request.getPropertyId());
        
        MessageDocument message = MessageDocument.builder()
                .senderId(senderId)
                .senderName(senderName)
                .senderEmail(senderEmail)
                .recipientId(request.getRecipientId())
                .recipientName(recipient.getName())
                .recipientEmail(recipient.getEmail())
                .propertyId(request.getPropertyId())
                .enquiryId(request.getEnquiryId())
                .content(request.getContent())
                .threadId(threadId)
                .read(false)
                .build();
        
        MessageDocument saved = messageRepository.save(message);
        log.info("Message sent with ID: {}", saved.getId());
        
        // Send notification to recipient
        String preview = request.getContent().length() > 50 ? 
                request.getContent().substring(0, 50) + "..." : 
                request.getContent();
        notificationService.notifyNewMessage(
                request.getRecipientId(), 
                recipient.getEmail(), 
                recipient.getName(), 
                senderName, 
                preview
        );
        
        return mapToResponse(saved);
    }
    
    @Override
    public List<MessageResponse> getConversation(String threadId, String userId) {
        log.info("Fetching conversation thread: {} for user: {}", threadId, userId);
        
        List<MessageDocument> messages = messageRepository.findByThreadIdOrderByCreatedAtDesc(threadId);
        
        // Mark all messages as read for this user
        messages.stream()
                .filter(m -> m.getRecipientId().equals(userId) && !m.isRead())
                .forEach(m -> {
                    m.setRead(true);
                    messageRepository.save(m);
                });
        
        return messages.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public PageResponse<MessageResponse> getReceivedMessages(String userId, Pageable pageable) {
        log.info("Fetching received messages for user: {}", userId);
        
        Page<MessageDocument> page = messageRepository.findByRecipientId(userId, pageable);
        
        var content = page.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        
        return PageResponse.<MessageResponse>builder()
                .content(content)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .hasNext(!page.isLast())
                .hasPrevious(page.getNumber() > 0)
                .build();
    }
    
    @Override
    public PageResponse<MessageResponse> getSentMessages(String userId, Pageable pageable) {
        log.info("Fetching sent messages for user: {}", userId);
        
        Page<MessageDocument> page = messageRepository.findBySenderId(userId, pageable);
        
        var content = page.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        
        return PageResponse.<MessageResponse>builder()
                .content(content)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .hasNext(!page.isLast())
                .hasPrevious(page.getNumber() > 0)
                .build();
    }
    
    @Override
    public PageResponse<MessageResponse> getUnreadMessages(String userId, Pageable pageable) {
        log.info("Fetching unread messages for user: {}", userId);
        
        Page<MessageDocument> page = messageRepository.findByRecipientIdAndRead(userId, false, pageable);
        
        var content = page.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        
        return PageResponse.<MessageResponse>builder()
                .content(content)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .hasNext(!page.isLast())
                .hasPrevious(page.getNumber() > 0)
                .build();
    }
    
    @Override
    public long getUnreadCount(String userId) {
        return messageRepository.countByRecipientIdAndRead(userId, false);
    }
    
    @Override
    public void markMessageAsRead(String messageId) {
        log.info("Marking message {} as read", messageId);
        
        MessageDocument message = messageRepository.findById(messageId)
                .orElseThrow(() -> new NotFoundException("Message not found"));
        
        message.setRead(true);
        messageRepository.save(message);
    }
    
    @Override
    public void markAllAsRead(String userId) {
        log.info("Marking all messages as read for user: {}", userId);
        
        Page<MessageDocument> unreadMessages = messageRepository
                .findByRecipientIdAndRead(userId, false, org.springframework.data.domain.Pageable.unpaged());
        
        unreadMessages.getContent().forEach(message -> {
            message.setRead(true);
            messageRepository.save(message);
        });
    }
    
    @Override
    public ConversationResponse getConversationWithUser(String userId, String otherUserId, String propertyId) {
        log.info("Fetching conversation between {} and {} for property {}", userId, otherUserId, propertyId);
        
        String threadId = generateThreadId(userId, otherUserId, propertyId);
        
        List<MessageDocument> messages = messageRepository.findByThreadIdOrderByCreatedAtDesc(threadId);
        
        // Mark all messages as read for this user
        messages.stream()
                .filter(m -> m.getRecipientId().equals(userId) && !m.isRead())
                .forEach(m -> {
                    m.setRead(true);
                    messageRepository.save(m);
                });
        
        UserDocument otherUser = userRepository.findById(otherUserId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        
        MessageDocument lastMessage = messages.isEmpty() ? null : messages.get(0);
        long unreadCount = messageRepository.countByRecipientIdAndRead(userId, false);
        
        return ConversationResponse.builder()
                .threadId(threadId)
                .otherUserId(otherUserId)
                .otherUserName(otherUser.getName())
                .otherUserEmail(otherUser.getEmail())
                .propertyId(propertyId)
                .lastMessage(lastMessage != null ? lastMessage.getContent() : null)
                .lastMessageTime(lastMessage != null ? lastMessage.getCreatedAt() : null)
                .unreadCount(unreadCount)
                .messages(messages.stream().map(this::mapToResponse).collect(Collectors.toList()))
                .build();
    }
    
    @Override
    public void deleteMessage(String messageId, String userId) {
        log.info("Deleting message {} by user {}", messageId, userId);
        
        MessageDocument message = messageRepository.findById(messageId)
                .orElseThrow(() -> new NotFoundException("Message not found"));
        
        // Only sender or recipient can delete
        if (!message.getSenderId().equals(userId) && !message.getRecipientId().equals(userId)) {
            throw new IllegalArgumentException("You don't have permission to delete this message");
        }
        
        messageRepository.deleteById(messageId);
    }
    
    @Override
    public PageResponse<ConversationResponse> getUserConversations(String userId, Pageable pageable) {
        log.info("Fetching conversations for user: {}", userId);
        
        Page<MessageDocument> page = messageRepository.findUserConversations(userId, pageable);
        
        // Group messages by threadId and create conversation responses
        var conversations = page.getContent().stream()
                .collect(Collectors.groupingBy(MessageDocument::getThreadId))
                .values().stream()
                .map(messages -> {
                    MessageDocument lastMsg = messages.stream()
                            .max(java.util.Comparator.comparing(MessageDocument::getCreatedAt))
                            .orElse(null);
                    
                    if (lastMsg == null) return null;
                    
                    String otherUserId = lastMsg.getSenderId().equals(userId) ? 
                            lastMsg.getRecipientId() : lastMsg.getSenderId();
                    
                    UserDocument otherUser = userRepository.findById(otherUserId)
                            .orElse(null);
                    
                    if (otherUser == null) return null;
                    
                    return ConversationResponse.builder()
                            .threadId(lastMsg.getThreadId())
                            .otherUserId(otherUserId)
                            .otherUserName(otherUser.getName())
                            .otherUserEmail(otherUser.getEmail())
                            .propertyId(lastMsg.getPropertyId())
                            .lastMessage(lastMsg.getContent())
                            .lastMessageTime(lastMsg.getCreatedAt())
                            .unreadCount(messages.stream()
                                    .filter(m -> m.getRecipientId().equals(userId) && !m.isRead())
                                    .count())
                            .build();
                })
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
        
        return PageResponse.<ConversationResponse>builder()
                .content(conversations)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .hasNext(!page.isLast())
                .hasPrevious(page.getNumber() > 0)
                .build();
    }
    
    private String generateThreadId(String userId1, String userId2, String propertyId) {
        // Create consistent thread ID regardless of message direction
        String[] userIds = {userId1, userId2};
        java.util.Arrays.sort(userIds);
        return userIds[0] + "_" + userIds[1] + "_" + (propertyId != null ? propertyId : "general");
    }
    
    private MessageResponse mapToResponse(MessageDocument message) {
        return MessageResponse.builder()
                .id(message.getId())
                .senderId(message.getSenderId())
                .senderName(message.getSenderName())
                .senderEmail(message.getSenderEmail())
                .recipientId(message.getRecipientId())
                .recipientName(message.getRecipientName())
                .recipientEmail(message.getRecipientEmail())
                .propertyId(message.getPropertyId())
                .enquiryId(message.getEnquiryId())
                .content(message.getContent())
                .read(message.isRead())
                .threadId(message.getThreadId())
                .createdAt(message.getCreatedAt())
                .build();
    }
}
