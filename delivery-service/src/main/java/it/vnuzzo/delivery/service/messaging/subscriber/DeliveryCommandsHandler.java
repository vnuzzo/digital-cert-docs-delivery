package it.vnuzzo.delivery.service.messaging.subscriber;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.vnuzzo.delivery.service.service.DeliveryOrchestrator;
import it.vnuzzo.delivery.service.utility.HeaderUtility;
import it.vnuzzo.shared.events.EventHeaders;
import it.vnuzzo.shared.events.delivery.DeliveryStartProcessingCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeliveryCommandsHandler {

    private final ObjectMapper objectMapper;
    private final DeliveryOrchestrator orchestrator;

    public Mono<Void> handleDeliveryCommand(Message<byte[]> message) {

        String eventId = HeaderUtility.headerAsString(message, EventHeaders.EVENT_ID);
        String eventType = HeaderUtility.headerAsString(message, EventHeaders.EVENT_TYPE);
        String eventTime = HeaderUtility.headerAsString(message, EventHeaders.EVENT_TIME);
        String traceId = HeaderUtility.headerAsString(message, EventHeaders.TRACE_ID);
        String correlationId = HeaderUtility.headerAsString(message, EventHeaders.CORRELATION_ID);

        if (eventId == null || eventType == null) {
            log.warn("Missing eventId/eventType. headers={}", message.getHeaders());
            return Mono.empty();
        }

        if (!"DeliveryStartProcessing".equals(eventType)) {
            log.debug("Ignoring message with eventType={}", eventType);
            return Mono.empty();
        }

        String json = new String(message.getPayload(), StandardCharsets.UTF_8);

        log.info("Received DeliveryStartProcessingCommand [eventId: {}]", eventId);

        return Mono.fromCallable(() -> objectMapper.readValue(json, DeliveryStartProcessingCommand.class))

                .subscribeOn(Schedulers.boundedElastic())

                .onErrorResume(ex -> {
                    log.error("Deserialization failed for eventId: {}", eventId, ex);
                    return Mono.empty();
                })

                .flatMap(command -> {

                    var context = new DeliveryOrchestrator.DeliveryMessageContext(
                            eventId,
                            eventType,
                            eventTime,
                            traceId,
                            correlationId != null? correlationId : command.requestId()
                    );

                    return orchestrator.process(command, context)
                            .doOnError(ex -> log.error("Delivery process failed [requestId: {} - eventId: {}]", command.requestId(), eventId, ex));
                })

                .then();
    }
}
