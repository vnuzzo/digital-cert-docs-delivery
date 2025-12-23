package it.vnuzzo.request.service.data.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "outbox_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutboxEvent {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "event_type", length = 64, nullable = false)
    private String eventType;

    @Column(name = "destination", length = 64, nullable = false)
    private String destination;

    @Column(name = "key_value", length = 36, nullable = false)
    private String requestId;

    @Column(name = "payload", columnDefinition = "json", nullable = false)
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 16, nullable = false)
    private OutboxStatus status;

    @Column(name = "attempts", nullable = false)
    private int attempts;

    @Column(name = "next_attempt_at")
    private Instant nextAttemptAt;

    @Column(name = "last_error", length = 512)
    private String lastError;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "sent_at")
    private Instant sentAt;

    public enum OutboxStatus {
        PENDING,
        IN_PROGRESS,
        SENT,
        DEAD
    }

}
