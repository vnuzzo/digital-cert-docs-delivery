package it.vnuzzo.notification.service.port;

import reactor.core.publisher.Mono;

public interface InternalServicePort {

    public Mono<Boolean> sendEventToInternalService(String requestId, String oldStatus, String newStatus,
                                                                    String reason, String errorMessage);

}
