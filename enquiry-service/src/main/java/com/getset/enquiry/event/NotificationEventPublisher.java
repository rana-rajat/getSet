package com.getset.enquiry.event;

import com.getset.common.event.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventPublisher {

    private final KafkaTemplate<String, NotificationEvent> kafkaTemplate;
    private static final String TOPIC = "notification-events";

    public void publish(NotificationEvent event) {
        kafkaTemplate.send(TOPIC, event.getRecipientId(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish {} event for {}", event.getEventType(), event.getRecipientId(),
                                ex);
                    } else {
                        log.debug("Published {} event for {}", event.getEventType(), event.getRecipientId());
                    }
                });
    }
}
