package com.getset.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Domain event published to Kafka whenever a notification needs to be sent.
 * The consumer (NotificationEventConsumer) picks this up asynchronously and
 * delegates to NotificationService — completely decoupled from the HTTP
 * request.
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

    /** Type of the event — drives routing logic in the consumer */
    private EventType eventType;

    // ── Recipient ────────────────────────────────────────────────────────────
    private String recipientId;
    private String recipientEmail;
    private String recipientName;

    // ── Sender / Actor ───────────────────────────────────────────────────────
    private String actorName; // e.g. renter name, owner name, sender name

    // ── Property context ─────────────────────────────────────────────────────
    private String propertyId;
    private String propertyTitle;

    // ── Extra payload ────────────────────────────────────────────────────────
    /** Rejection reason (ENQUIRY_REJECTED) or message preview (MESSAGE_RECEIVED) */
    private String extraPayload;

    /**
     * Related entity ID for the notification document (enquiry ID, message ID,
     * etc.)
     */
    private String relatedEntityId;
}
