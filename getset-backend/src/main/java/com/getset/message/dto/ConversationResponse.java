package com.getset.message.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
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
    
    @Schema(description = "Unique thread ID for the conversation")
    private String threadId;
    
    @Schema(description = "ID of the other participant")
    private String otherUserId;
    
    @Schema(description = "Name of the other participant")
    private String otherUserName;
    
    @Schema(description = "Email of the other participant")
    private String otherUserEmail;
    
    @Schema(description = "Associated property ID")
    private String propertyId;
    
    @Schema(description = "Last message content")
    private String lastMessage;
    
    @Schema(description = "Timestamp of the last message")
    private Instant lastMessageTime;
    
    @Schema(description = "Number of unread messages")
    private long unreadCount;
    
    @Schema(description = "List of messages in this conversation")
    private List<MessageResponse> messages;
}
