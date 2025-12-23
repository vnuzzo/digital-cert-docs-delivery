package it.vnuzzo.request.service.messaging.subscriber;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.vnuzzo.request.service.data.entity.ConsumedEvent;
import it.vnuzzo.request.service.data.entity.OutboxEvent;
import it.vnuzzo.request.service.data.entity.Request;
import it.vnuzzo.request.service.data.repository.ConsumedEventRepository;
import it.vnuzzo.request.service.data.repository.OutboxEventRepository;
import it.vnuzzo.request.service.data.repository.RequestRepository;
import it.vnuzzo.shared.enums.EventType;
import it.vnuzzo.shared.enums.FailureCode;
import it.vnuzzo.shared.enums.RequestStatus;
import it.vnuzzo.shared.events.EventHeaders;
import it.vnuzzo.shared.events.delivery.DeliveryCompletedEvent;
import it.vnuzzo.shared.events.delivery.DeliveryFailedEvent;
import it.vnuzzo.shared.events.delivery.DeliveryProcessingStartedEvent;
import it.vnuzzo.shared.events.request.RequestStatusChangedEvent;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@Component
public class DeliveryResultHandler {

    private static final String REQUEST_EVENTS_BINDING = "requestEvents-out-0";

    private final ObjectMapper objectMapper;
    private final RequestRepository requestRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ConsumedEventRepository consumedEventRepository;

    private final String consumerName;

    public DeliveryResultHandler(ObjectMapper objectMapper,
                                 RequestRepository requestRepository,
                                 OutboxEventRepository outboxEventRepository,
                                 ConsumedEventRepository consumedEventRepository,
                                 @Value("app.request.consumer-name:request-service:delivery.events") String consumerName) {
        this.objectMapper = objectMapper;
        this.requestRepository = requestRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.consumedEventRepository = consumedEventRepository;
        this.consumerName = consumerName;
    }

    @Transactional
    public void handle(Message<byte[]> message) {

        String eventId = headerAsString(message, EventHeaders.EVENT_ID);
        String eventType = headerAsString(message, EventHeaders.EVENT_TYPE);
        String eventTime = headerAsString(message, EventHeaders.EVENT_TIME);

        log.info("Handling message EventId: {} and EventType: {}", eventId, eventType);

        if (eventId == null || eventType == null) {
            log.warn("Missing mandatory headers [actual headers: {}]", message.getHeaders());
            return;
        }

        if (!registerConsumedEvent(eventId)) {
            log.debug("Trying to consume already delivered event [eventId: {}]", eventId);
            return;
        }

        byte[] payloadBytes = message.getPayload();
        String payloadJson = new String(payloadBytes, StandardCharsets.UTF_8);

        try {

            if (EventType.DELIVERY_STARTED_PROCESSING.getEventType().equals(eventType)) {
                DeliveryProcessingStartedEvent deliveryProcessingStartedEvent = objectMapper.readValue(payloadJson, DeliveryProcessingStartedEvent.class);
                onDeliveryProcessingStarted(deliveryProcessingStartedEvent, eventTime);
                return;
            }

            if (EventType.DELIVERY_COMPLETED.getEventType().equals(eventType)) {
                DeliveryCompletedEvent deliveryCompletedEvent = objectMapper.readValue(payloadJson, DeliveryCompletedEvent.class);
                onDeliveryCompleted(deliveryCompletedEvent, eventTime);
                return;
            }

            if (EventType.DELIVERY_FAILED.getEventType().equals(eventType)) {
                DeliveryFailedEvent deliveryFailedEvent = objectMapper.readValue(payloadJson, DeliveryFailedEvent.class);
                onDeliveryFailed(deliveryFailedEvent, eventTime);
                return;
            }

            log.warn("Unknown eventType can't handle event [eventId: {} - eventType: {}]", eventId, eventType);

        } catch (Exception ex) {
            log.error("Exception during routing and working event [eventType: {} - eventId: {} - eventPayload: {}", eventType, eventId, payloadJson, ex);
            throw new IllegalStateException("Exception during routing and working event [eventId: " + eventId + "]", ex);
        }
    }

    private boolean registerConsumedEvent(String eventId) {
        try {
            consumedEventRepository.save(ConsumedEvent.builder()
                    .consumerName(consumerName)
                    .eventId(eventId)
                    .receivedAt(Instant.now())
                    .build());
            return true;
        } catch (DataIntegrityViolationException dup) {
            return false;
        }
    }

    private void onDeliveryProcessingStarted(DeliveryProcessingStartedEvent ev, String eventTime) {

        String requestId = ev.requestId();

        Request req = requestRepository.findById(requestId).orElse(null);

        if (req == null) {
            log.warn("Request not found for event DeliveryProcessingStarted [requestId: {}]", requestId);
            return;
        }

        if (req.getStatus() != RequestStatus.IN_ATTESA) {
            log.info("Failed to apply DeliveryProcessingStarted on request not in state IN_ATTES [requestId: {} - requestStatus: {}]", requestId, req.getStatus());
            return;
        }

        Instant now = Instant.now();
        RequestStatus old = req.getStatus();

        req.setStatus(RequestStatus.IN_ELABORAZIONE);
        req.setUpdatedAt(now);
        requestRepository.save(req);

        enqueueStatusChanged(requestId, old, RequestStatus.IN_ELABORAZIONE, "Delivery Service started processing event", null, eventTime);
        log.info("Applied changeState to IN_ELABORAZIONE [requestId: {}]", requestId);
    }

    private void onDeliveryCompleted(DeliveryCompletedEvent ev, String eventTime) {

        String requestId = ev.requestId();

        Request req = requestRepository.findById(requestId).orElse(null);

        if (req == null) {
            log.warn("Request not found for event DeliveryCompleted [requestId: {}]", requestId);
            return;
        }

        if (req.getStatus() != RequestStatus.IN_ELABORAZIONE) {
            log.info("Failed to apply DeliveryCompleted on request not in state IN_ELABORAZIONE [requestId: {} - requestStatus: {}]", requestId, req.getStatus());
            return;
        }

        Instant now = Instant.now();
        RequestStatus old = req.getStatus();

        req.setStatus(RequestStatus.COMPLETATA);
        req.setUpdatedAt(now);
        req.setFailureCode(null);
        req.setFailureMessage(null);
        requestRepository.save(req);

        enqueueStatusChanged(requestId, old, RequestStatus.COMPLETATA, "Delivery service completed", null, eventTime);
        log.info("Applied changeState to COMPLETATA [requestId: {}]", requestId);
    }

    private void onDeliveryFailed(DeliveryFailedEvent ev, String eventTime) {

        String requestId = ev.requestId();

        Request req = requestRepository.findById(requestId).orElse(null);
        if (req == null) {
            log.warn("Request not found for event DeliveryFailed [requestId: {}]", requestId);
            return;
        }

        if (req.getStatus() != RequestStatus.IN_ATTESA && req.getStatus() != RequestStatus.IN_ELABORAZIONE) {
            log.info("Failed to apply DeliveryFailed on request not in state IN_ATTESA or IN_ELABORAZIONE [requestId: {} - requestStatus: {}]", requestId, req.getStatus());
            return;
        }

        Instant now = Instant.now();
        RequestStatus old = req.getStatus();

        req.setStatus(RequestStatus.FALLITA);
        req.setUpdatedAt(now);
        req.setFailureCode(ev.failureCode() != null ? ev.failureCode().name() : FailureCode.DELIVERY_FAILED.name());
        req.setFailureMessage(ev.failureMessage());
        requestRepository.save(req);

        enqueueStatusChanged(requestId, old, RequestStatus.FALLITA, req.getFailureCode(), ev.failureMessage(), eventTime);
        log.info("Applied changeState to FALLITA [requestId: {} - failureCode: {}]", requestId, req.getFailureCode());
    }

    private void enqueueStatusChanged(String requestId,
                                      RequestStatus oldStatus,
                                      RequestStatus newStatus,
                                      String reason,
                                      String errorMessage,
                                      String eventTime) {

        RequestStatusChangedEvent payload = new RequestStatusChangedEvent(
                requestId,
                oldStatus,
                newStatus,
                reason,
                errorMessage,
                Instant.parse(eventTime)
        );

        String json;
        try {
            json = objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot serialize RequestStatusChangedEvent", e);
        }

        OutboxEvent outbox = OutboxEvent.builder()
                .id(UUID.randomUUID().toString())
                .eventType(EventType.REQUEST_STATUS_CHANGED.getEventType())
                .destination(REQUEST_EVENTS_BINDING)
                .requestId(requestId)
                .payload(json)
                .status(OutboxEvent.OutboxStatus.PENDING)
                .attempts(0)
                .createdAt(Instant.now())
                .build();

        outboxEventRepository.save(outbox);
    }

    private String headerAsString(Message<?> message, String headerName) {
        Object v = message.getHeaders().get(headerName);
        if (v == null) return null;

        if (v instanceof byte[] bytes) {
            return new String(bytes, StandardCharsets.UTF_8);
        }
        return v.toString();
    }
}
