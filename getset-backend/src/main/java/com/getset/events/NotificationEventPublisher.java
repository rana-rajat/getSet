package com.getset.events;

import com.getset.config.KafkaConfig;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Publishes NotificationEvents to Kafka asynchronously.
 * Services (EnquiryServiceImpl, MessageServiceImpl) should call this instead of
 * NotificationService directly, ensuring the HTTP request returns immediately
 * without waiting for email delivery.
 */
@Slf4j
@Component
public class NotificationEventPublisher {

    private final KafkaTemplate<String, NotificationEvent> kafkaTemplate;
    private final Counter publishedCounter;
    private final Counter failedCounter;

    public NotificationEventPublisher(
            KafkaTemplate<String, NotificationEvent> kafkaTemplate,
            MeterRegistry meterRegistry) {
        this.kafkaTemplate = kafkaTemplate;
        this.publishedCounter = Counter.builder("getset.kafka.events.published")
                .description("Number of notification events successfully published to Kafka")
                .tag("topic", KafkaConfig.NOTIFICATION_TOPIC)
                .register(meterRegistry);
        this.failedCounter = Counter.builder("getset.kafka.events.failed")
                .description("Number of notification events that failed to publish")
                .tag("topic", KafkaConfig.NOTIFICATION_TOPIC)
                .register(meterRegistry);
    }

    /**
     * Publishes a notification event to the Kafka topic asynchronously.
     * Uses the recipientId as the message key to ensure ordering per recipient.
     */
    public void publish(NotificationEvent event) {
        String key = event.getRecipientId() != null ? event.getRecipientId() : "unknown";

        CompletableFuture<SendResult<String, NotificationEvent>> future = kafkaTemplate
                .send(KafkaConfig.NOTIFICATION_TOPIC, key, event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                failedCounter.increment();
                log.error("Failed to publish {} event for recipient {}. Falling back to sync notification.",
                        event.getEventType(), event.getRecipientEmail(), ex);
            } else {
                publishedCounter.increment();
                log.debug("Published {} event to partition {} offset {}",
                        event.getEventType(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
    }
}
