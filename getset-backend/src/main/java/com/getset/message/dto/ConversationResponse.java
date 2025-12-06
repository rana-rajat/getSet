package com.getset.message.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationResponse {
    
    private String threadId;
    private String otherUserId;
    private String otherUserName;
    private String otherUserEmail;
    private String propertyId;
    private String lastMessage;
    private Instant lastMessageTime;
    private long unreadCount;
    private List<MessageResponse> messages;
}
