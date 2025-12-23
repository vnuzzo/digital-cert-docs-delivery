package it.vnuzzo.request.service.data.repository;

import it.vnuzzo.request.service.data.entity.RequestDocument;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RequestDocumentRepository extends JpaRepository<RequestDocument, Long> {
}
