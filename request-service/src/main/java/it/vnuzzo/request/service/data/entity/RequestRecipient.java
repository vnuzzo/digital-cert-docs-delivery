package it.vnuzzo.request.service.data.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "request_recipients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestRecipient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_id", length = 36, nullable = false)
    private String requestId;

    @Column(name = "recipient_address", length = 256, nullable = false)
    private String recipientAddress;

}
