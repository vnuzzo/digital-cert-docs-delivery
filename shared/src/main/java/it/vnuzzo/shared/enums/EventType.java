package it.vnuzzo.shared.enums;

import lombok.Getter;

@Getter
public enum EventType {

    REQUEST_CREATED("RequestCreated"),
    REQUEST_STATUS_CHANGED("RequestStatusChanged"),
    DELIVERY_START_PROCESSING("DeliveryStartProcessing"),
    DELIVERY_STARTED_PROCESSING ("DeliveryProcessingStarted"),
    DELIVERY_COMPLETED("DeliveryCompleted"),
    DELIVERY_FAILED("DeliveryFailed");

    private final String eventType;

    EventType(String eventType) {
        this.eventType = eventType;
    }
}
