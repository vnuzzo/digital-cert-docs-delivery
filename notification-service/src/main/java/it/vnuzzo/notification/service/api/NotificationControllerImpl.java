package it.vnuzzo.notification.service.api;

import it.vnuzzo.notification.service.api.dto.NotificationResponse;
import it.vnuzzo.notification.service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationControllerImpl implements NotificationController {

    private final NotificationService notificationService;

    @Override
    @GetMapping("/{id}")
    public Flux<NotificationResponse> getNotificationStatusByRequestId(@PathVariable("id") String requestId) {
        log.info("Received request getNotificationStatusByRequestId for requestId: {}", requestId);
        return notificationService.getNotificationStatusByRequestId(requestId);
    }
}
