package com.getset.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Domain event published to Kafka topic "notification-events".
 * Published by: enquiry-service, message-service
 * Consumed by: notification-service
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent {

    public enum EventType {
        ENQUIRY_RECEIVED,
        ENQUIRY_ACCEPTED,
        ENQUIRY_REJECTED,
        MESSAGE_RECEIVED
    }

    private EventType eventType;
    private String recipientId;
    private String recipientEmail;
    private String recipientName;
    private String actorName;
    private String propertyId;
    private String propertyTitle;
    /** rejection reason or message preview depending on event type */
    private String extraPayload;
    private String relatedEntityId;
}
