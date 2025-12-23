package it.vnuzzo.request.service.data.repository;

import it.vnuzzo.request.service.data.entity.OutboxEvent;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, String> {

    @Query(value = """
            select e.id
            from outbox_events e
            where e.status = 'PENDING'
              and (e.next_attempt_at is null or e.next_attempt_at <= :now)
            order by e.created_at asc
            """, nativeQuery = true)
    List<String> findPublishableIds(@Param("now") Instant now, Pageable pageable);

    @Modifying
    @Query(value = """
            update outbox_events
            set status = 'IN_PROGRESS'
            where id = :id and status = 'PENDING'
            and (next_attempt_at is null or next_attempt_at <= now())
            """, nativeQuery = true)
    int claim(@Param("id") String id);

    @Modifying
    @Query(value = """
            update outbox_events
            set status = 'SENT',
                sent_at = :sentAt,
                last_error = null
            where id = :id and status = 'IN_PROGRESS'
            """, nativeQuery = true)
    void updateStatusSent(@Param("id") String id, @Param("sentAt") Instant sentAt);

    @Modifying
    @Query(value = """
            update outbox_events
            set status = 'PENDING',
                attempts = attempts + 1,
                next_attempt_at = :nextAttemptAt,
                last_error = :lastError
            where id = :id and status = 'IN_PROGRESS'
            """, nativeQuery = true)
    void reschedule(@Param("id") String id,
                   @Param("nextAttemptAt") Instant nextAttemptAt,
                   @Param("lastError") String lastError);

    @Modifying
    @Query(value = """
            update outbox_events
            set status = 'DEAD',
                attempts = attempts + 1,
                last_error = :lastError
            where id = :id and status = 'IN_PROGRESS'
            """, nativeQuery = true)
    void updateStatusDead(@Param("id") String id, @Param("lastError") String lastError);

}
