package it.vnuzzo.recipient.service.data.entity;

import it.vnuzzo.recipient.service.data.entity.enums.RecipientStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "recipients_status_audit")
public class RecipientStatusAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "recipient_id", length = 36, nullable = false)
    private String recipientId;

    @Enumerated(EnumType.STRING)
    @Column(name = "old_status", length = 16, nullable = false)
    private RecipientStatus oldStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", length = 16, nullable = false)
    private RecipientStatus newStatus;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

}
