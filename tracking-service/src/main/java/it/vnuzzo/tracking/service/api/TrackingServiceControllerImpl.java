package it.vnuzzo.tracking.service.api;

import it.vnuzzo.tracking.service.api.dto.TrackingTimelineEventResponse;
import it.vnuzzo.tracking.service.service.TrackingEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/tracking")
@RequiredArgsConstructor
public class TrackingServiceControllerImpl implements TrackingEventController {

    private final TrackingEventService trackingEventService;

    @Override
    @GetMapping("/{id}")
    public Mono<TrackingTimelineEventResponse> getTrackingForRequest(@PathVariable("id") String requestId) {
        log.info("Received request getTrackingForRequest for requestId: {}", requestId);
        return trackingEventService.getTrackingForRequest(requestId);
    }
}
