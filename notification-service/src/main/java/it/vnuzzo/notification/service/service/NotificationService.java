package it.vnuzzo.notification.service.service;

import it.vnuzzo.notification.service.api.dto.NotificationResponse;
import it.vnuzzo.notification.service.data.repository.NotificationAttemptRepository;
import it.vnuzzo.shared.exceptions.ApplicationException;
import it.vnuzzo.shared.exceptions.RepositoryException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationAttemptRepository notificationAttemptRepository;

    public Flux<NotificationResponse> getNotificationStatusByRequestId(String requestId) {

        log.info("Retrieving notification attempts for requestId: {}", requestId);

        return notificationAttemptRepository.findByRequestIdOrderByOccurredAtAsc(requestId)

                .map(notificationAttempt -> new NotificationResponse(
                        notificationAttempt.requestId(),
                        notificationAttempt.eventId(),
                        notificationAttempt.requestStatus(),
                        notificationAttempt.notificationStatus(),
                        notificationAttempt.occurredAt(),
                        notificationAttempt.receivedAt()
                ))

                .doOnNext(notificationResponse ->
                        log.debug("Found notification attempt: {}", notificationResponse)
                )
                .switchIfEmpty(Flux.defer(() -> {

                    log.info("No notification attempts found for requestId: {}", requestId);
                    return Flux.error(new ApplicationException("No notification attempts found for requestId: " + requestId, "NOT_FOUND"));

                }))

                .onErrorResume(ex -> {

                    if(ex instanceof ApplicationException)
                        return Flux.error(ex);

                    log.error("Error retrieving notification attempts for requestId: {}", requestId, ex);
                    return Flux.error(new RepositoryException("Failed to retrieve notification attempts", ex));

                });
    }

}
