package com.getset.notification.consumer;

import com.getset.common.event.NotificationEvent;
import com.getset.notification.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Kafka consumer — the heart of notification-service.
 * Receives events from enquiry-service and message-service and sends emails.
 * Completely stateless — all needed data is in the event payload.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventConsumer {

    private final EmailService emailService;

    @KafkaListener(topics = "notification-events", groupId = "notification-consumers")
    public void consume(
            @Payload NotificationEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        log.info("Consuming {} event from partition={} offset={} recipient={}",
                event.getEventType(), partition, offset, event.getRecipientEmail());
        try {
            switch (event.getEventType()) {
                case ENQUIRY_RECEIVED -> emailService.sendEnquiryReceivedEmail(
                        event.getRecipientEmail(), event.getRecipientName(),
                        event.getActorName(), event.getPropertyTitle(), event.getExtraPayload());
                case ENQUIRY_ACCEPTED -> emailService.sendEnquiryAcceptedEmail(
                        event.getRecipientEmail(), event.getRecipientName(),
                        event.getActorName(), event.getPropertyTitle());
                case ENQUIRY_REJECTED -> emailService.sendEnquiryRejectedEmail(
                        event.getRecipientEmail(), event.getRecipientName(),
                        event.getActorName(), event.getPropertyTitle(), event.getExtraPayload());
                case MESSAGE_RECEIVED -> emailService.sendNewMessageEmail(
                        event.getRecipientEmail(), event.getRecipientName(),
                        event.getActorName(), event.getExtraPayload());
                default -> log.warn("Unknown event type: {}", event.getEventType());
            }
        } catch (Exception ex) {
            log.error("Failed to process {} for {}", event.getEventType(), event.getRecipientEmail(), ex);
        }
    }
}
