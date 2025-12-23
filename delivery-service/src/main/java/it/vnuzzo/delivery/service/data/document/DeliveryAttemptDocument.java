package it.vnuzzo.delivery.service.data.document;

import it.vnuzzo.shared.enums.DeliveryType;
import it.vnuzzo.shared.enums.FailureCode;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document("delivery_attempts")
public class DeliveryAttemptDocument {

        @Id String id;
        String requestId;
        DeliveryStatus status;
        DeliveryType deliveryType;
        List<String> recipients;
        List<String> documents;
        String providerMessageId;
        FailureCode failureCode;
        String failureMessage;
        Instant createdAt;
        Instant updatedAt;


    public enum DeliveryStatus {
        CONSUMED, VALIDATED, SENT, FAILED
    }

}
