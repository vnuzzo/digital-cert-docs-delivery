package it.vnuzzo.request.service.data.repository;

import it.vnuzzo.request.service.data.entity.RequestRecipient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RequestRecipientRepository extends JpaRepository<RequestRecipient, Long> {
}
