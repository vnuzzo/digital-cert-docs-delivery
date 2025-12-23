package it.vnuzzo.tracking.service.api;

import it.vnuzzo.tracking.service.api.dto.TrackingTimelineEventResponse;
import reactor.core.publisher.Mono;

public interface TrackingEventController {

    public Mono<TrackingTimelineEventResponse> getTrackingForRequest(String requestId);

}
