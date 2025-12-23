package it.vnuzzo.tracking.service.service;

import it.vnuzzo.tracking.service.api.dto.TrackingEventResponse;
import it.vnuzzo.tracking.service.api.dto.TrackingTimelineEventResponse;
import it.vnuzzo.tracking.service.data.document.TrackingEventDocument;
import it.vnuzzo.tracking.service.data.repository.TrackingEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrackingEventService {

    private final TrackingEventRepository trackingEventRepository;

    public Mono<TrackingTimelineEventResponse> getTrackingForRequest(String requestId) {

        log.info("Entered getTrackingForRequest for requestId: {}", requestId);

        return trackingEventRepository.findByRequestIdOrderByOccuredAtAsc(requestId)
                .collectList()
                .map(trackingEvents -> buildTrackingTimelineEventResponse(requestId, trackingEvents));

    }

    private TrackingTimelineEventResponse buildTrackingTimelineEventResponse(String requestId, List<TrackingEventDocument> trackingEvents) {

        log.debug("Entered buildTrackingTimelineEventResponse for requestId: {}", requestId);

        if(trackingEvents == null || trackingEvents.isEmpty()) {
            log.error("No tracking events found for requestId: {}", requestId);
            return new TrackingTimelineEventResponse(requestId, null,
                    null, null,
                    "NOT_FOUND", null);
        }

        var currentStatus = trackingEvents.getLast().status();
        var recipients = trackingEvents.getFirst().recipients();
        var documents = trackingEvents.getFirst().documents();
        var deliveryType = trackingEvents.getFirst().deliveryType();

        return new TrackingTimelineEventResponse(
                requestId,
                deliveryType,
                recipients,
                documents,
                currentStatus.name(),
                trackingEvents.stream().map(this::mapToTrackingEventResponse).toList()
        );

    }

    private TrackingEventResponse mapToTrackingEventResponse(TrackingEventDocument document) {
        return new TrackingEventResponse(
                document.eventId(),
                document.eventType(),
                document.eventTime(),
                document.status() != null ? document.status().name() : null,
                document.reason(),
                document.failureMessage(),
                document.payload(),
                document.receivedAt()
        );
    }

}
