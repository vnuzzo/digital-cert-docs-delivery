package it.vnuzzo.request.service.data.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "request_documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_id", length = 36, nullable = false)
    private String requestId;

    @Column(name = "document_ref", length = 512, nullable = false)
    private String documentRef;

}
