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
@Table(name = "recipients")
public class Recipient {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "digital_address", length = 256, nullable = false)
    private String digitalAddress;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 16, nullable = false)
    private RecipientStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

}
