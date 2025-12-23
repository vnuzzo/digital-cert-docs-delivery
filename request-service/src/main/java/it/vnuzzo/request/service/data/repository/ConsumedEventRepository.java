package it.vnuzzo.request.service.data.repository;

import it.vnuzzo.request.service.data.entity.ConsumedEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsumedEventRepository extends JpaRepository<ConsumedEvent, Long> {

}
