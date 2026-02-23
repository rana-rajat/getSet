package com.getset.events;

import com.getset.config.KafkaConfig;
import com.getset.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Kafka consumer for notification events.
 *
 * This runs on a background thread pool — completely decoupled from HTTP
 * request threads.
 * If email sending fails, the message can be retried (offset not committed).
 * If a malformed message arrives, the ErrorHandlingDeserializer in KafkaConfig
 * skips it and logs — the consumer never crashes.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventConsumer {

    private final NotificationService notificationService;

    @KafkaListener(topics = KafkaConfig.NOTIFICATION_TOPIC, groupId = "getset-notification-consumers", containerFactory = "kafkaListenerContainerFactory")
    public void consume(
            @Payload NotificationEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        log.info("Consuming {} event from partition={} offset={} for recipient={}",
                event.getEventType(), partition, offset, event.getRecipientEmail());

        try {
            route(event);
        } catch (Exception ex) {
            // Log the error but don't throw — prevents infinite retry loop
            // In production, wire a dead-letter topic here via SeekToCurrentErrorHandler
            log.error("Failed to process {} event for recipient {}. Event will be skipped.",
                    event.getEventType(), event.getRecipientEmail(), ex);
        }
    }

    private void route(NotificationEvent event) {
        switch (event.getEventType()) {
            case ENQUIRY_RECEIVED -> notificationService.notifyEnquiryReceived(
                    event.getRecipientEmail(), // ownerEmail
                    event.getRecipientName(), // ownerName
                    event.getRelatedEntityId(), // renterId
                    event.getActorName(), // renterName
                    event.getPropertyId(),
                    event.getPropertyTitle(),
                    event.getExtraPayload() // enquiry message
                );
            case ENQUIRY_ACCEPTED -> notificationService.notifyEnquiryAccepted(
                    event.getRecipientEmail(), // renterEmail
                    event.getRecipientName(), // renterName
                    event.getActorName(), // ownerName
                    event.getPropertyId(),
                    event.getPropertyTitle());
            case ENQUIRY_REJECTED -> notificationService.notifyEnquiryRejected(
                    event.getRecipientEmail(), // renterEmail
                    event.getRecipientName(), // renterName
                    event.getActorName(), // ownerName
                    event.getPropertyId(),
                    event.getPropertyTitle(),
                    event.getExtraPayload() // rejection reason
                );
            case MESSAGE_RECEIVED -> notificationService.notifyNewMessage(
                    event.getRecipientId(),
                    event.getRecipientEmail(),
                    event.getRecipientName(),
                    event.getActorName(), // senderName
                    event.getExtraPayload() // message preview
                );
            default -> log.warn("Unknown event type: {}", event.getEventType());
        }
    }
}
