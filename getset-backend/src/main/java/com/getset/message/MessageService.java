package com.getset.message;

import com.getset.common.PageResponse;
import com.getset.message.dto.ConversationResponse;
import com.getset.message.dto.MessageRequest;
import com.getset.message.dto.MessageResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MessageService {
    
    /**
     * Send a message
     */
    MessageResponse sendMessage(MessageRequest request, String senderId, String senderName, String senderEmail);
    
    /**
     * Get messages in a conversation thread
     */
    List<MessageResponse> getConversation(String threadId, String userId);
    
    /**
     * Get all messages for a user (received)
     */
    PageResponse<MessageResponse> getReceivedMessages(String userId, Pageable pageable);
    
    /**
     * Get all messages sent by a user
     */
    PageResponse<MessageResponse> getSentMessages(String userId, Pageable pageable);
    
    /**
     * Get unread messages
     */
    PageResponse<MessageResponse> getUnreadMessages(String userId, Pageable pageable);
    
    /**
     * Get unread count
     */
    long getUnreadCount(String userId);
    
    /**
     * Mark message as read
     */
    void markMessageAsRead(String messageId);
    
    /**
     * Mark all messages as read
     */
    void markAllAsRead(String userId);
    
    /**
     * Get conversation thread with another user (for property)
     */
    ConversationResponse getConversationWithUser(String userId, String otherUserId, String propertyId);
    
    /**
     * Delete message
     */
    void deleteMessage(String messageId, String userId);
    
    /**
     * Get all conversations for a user
     */
    PageResponse<ConversationResponse> getUserConversations(String userId, Pageable pageable);
}
