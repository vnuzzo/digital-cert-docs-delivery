package it.vnuzzo.request.service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.vnuzzo.request.service.api.dto.CreateRequestResponseDto;
import it.vnuzzo.request.service.data.entity.OutboxEvent;
import it.vnuzzo.request.service.data.entity.Request;
import it.vnuzzo.request.service.data.entity.RequestDocument;
import it.vnuzzo.request.service.data.entity.RequestRecipient;
import it.vnuzzo.request.service.data.repository.OutboxEventRepository;
import it.vnuzzo.request.service.data.repository.RequestDocumentRepository;
import it.vnuzzo.request.service.data.repository.RequestRecipientRepository;
import it.vnuzzo.request.service.data.repository.RequestRepository;
import it.vnuzzo.shared.enums.DeliveryType;
import it.vnuzzo.shared.enums.EventType;
import it.vnuzzo.shared.enums.RequestStatus;
import it.vnuzzo.shared.events.delivery.DeliveryStartProcessingCommand;
import it.vnuzzo.shared.events.request.RequestCreatedEvent;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RequestCreationService {

    public static final String TOPIC_REQUEST_EVENTS = "requestEvents-out-0";
    public static final String TOPIC_DELIVERY_COMMANDS = "deliveryCommands-out-0";

    private final RequestRepository requestRepository;
    private final RequestRecipientRepository recipientRepository;
    private final RequestDocumentRepository documentRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public CreateRequestResponseDto createRequest(DeliveryType deliveryType, List<String> recipients, List<String> documents) {

        log.info("Entered createRequest [deliveryType: {} - recipients: {} - documents: {}]", deliveryType, recipients, documents);

        String requestId = UUID.randomUUID().toString();
        Instant now = Instant.now();

        Request request = Request.builder()
                .id(requestId)
                .deliveryType(deliveryType)
                .status(RequestStatus.IN_ATTESA)
                .createdAt(now)
                .updatedAt(now)
                .build();

        requestRepository.save(request);

        log.info("Persisted request [requestId: {}]", request);

        List<RequestRecipient> recipientEntities = recipients.stream()
                .map(digitalAddress -> RequestRecipient.builder()
                        .requestId(requestId)
                        .recipientAddress(digitalAddress)
                        .build())
                .toList();

        recipientRepository.saveAll(recipientEntities);


        List<RequestDocument> documentEntities = documents.stream()
                .map(document -> RequestDocument.builder()
                        .requestId(requestId)
                        .documentRef(document)
                        .build())
                .toList();

        documentRepository.saveAll(documentEntities);


        // Building evento di richiesta creata (fini tracking)
        RequestCreatedEvent createdEvent = new RequestCreatedEvent(
                requestId,
                deliveryType,
                recipients.stream()
                        .map(String::trim)
                        .distinct()
                        .toList(),
                documents.stream()
                        .map(String::trim)
                        .distinct()
                        .toList(),
                RequestStatus.IN_ATTESA,
                now
        );

        // Building comando di delivery
        DeliveryStartProcessingCommand deliveryStartProcessingCommand = new DeliveryStartProcessingCommand(
                requestId,
                deliveryType,
                recipients.stream()
                        .map(String::trim)
                        .distinct()
                        .toList(),
                documents.stream()
                        .map(String::trim)
                        .distinct()
                        .toList(),
                now
        );

        // Persistenza outbox
        outboxEventRepository.saveAll(List.of(
                createOutbox(generateJson(createdEvent), EventType.REQUEST_CREATED.getEventType(), TOPIC_REQUEST_EVENTS, requestId, now),
                createOutbox(generateJson(deliveryStartProcessingCommand), EventType.DELIVERY_START_PROCESSING.getEventType(), TOPIC_DELIVERY_COMMANDS, requestId, now)
        ));

        log.info("Persisted outbox events for request created and delivery started [requestId: {}]", requestId);

        return new CreateRequestResponseDto(requestId, RequestStatus.IN_ATTESA);
    }

    private OutboxEvent createOutbox(String payloadJson, String eventType, String destination, String requestId, Instant now) {
        return OutboxEvent.builder()
                .id(UUID.randomUUID().toString())
                .eventType(eventType)
                .destination(destination)
                .requestId(requestId)
                .payload(payloadJson)
                .status(OutboxEvent.OutboxStatus.PENDING)
                .attempts(0)
                .createdAt(now)
                .build();
    }

    private String generateJson(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Serialization of payload failed", e);
        }
    }

}
