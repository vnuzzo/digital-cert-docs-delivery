package it.vnuzzo.notification.service.api;

import it.vnuzzo.notification.service.api.dto.NotificationResponse;
import reactor.core.publisher.Flux;

public interface NotificationController {

    Flux<NotificationResponse> getNotificationStatusByRequestId(String requestId);

}
