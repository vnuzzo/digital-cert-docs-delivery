package it.vnuzzo.request.service.data.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "consumed_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsumedEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "consumer_name", length = 64, nullable = false)
    private String consumerName;

    @Column(name = "event_id", length = 36, nullable = false)
    private String eventId;

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;

}
