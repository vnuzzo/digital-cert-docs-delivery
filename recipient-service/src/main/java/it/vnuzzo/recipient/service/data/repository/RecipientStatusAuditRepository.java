package it.vnuzzo.recipient.service.data.repository;

import it.vnuzzo.recipient.service.data.entity.RecipientStatusAudit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipientStatusAuditRepository extends JpaRepository<RecipientStatusAudit, Long> {
}
