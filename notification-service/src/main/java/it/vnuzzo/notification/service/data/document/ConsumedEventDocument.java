package it.vnuzzo.notification.service.data.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "consumed_events")
public record ConsumedEventDocument (

        @Id String id,
        String eventId,
        String consumerName,
        String requestId,
        Instant receivedAt

) {}
