package it.vnuzzo.request.service.data.entity;

import it.vnuzzo.shared.enums.DeliveryType;
import it.vnuzzo.shared.enums.RequestStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Request {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_type", length = 16, nullable = false)
    private DeliveryType deliveryType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 24, nullable = false)
    private RequestStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "failure_code", length = 32)
    private String failureCode;

    @Column(name = "failure_message", length = 512)
    private String failureMessage;

}
