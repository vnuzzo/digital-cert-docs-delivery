package it.vnuzzo.recipient.service.data.repository;

import it.vnuzzo.recipient.service.data.entity.Recipient;
import it.vnuzzo.recipient.service.data.entity.enums.RecipientStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface RecipientRepository extends JpaRepository<Recipient, String> {

    Optional<Recipient> findByDigitalAddress(String digitalAddress);

    List<Recipient> findByDigitalAddressIn(Set<String> digitalAddresses);

    List<Recipient> findByStatus(RecipientStatus status);

}
