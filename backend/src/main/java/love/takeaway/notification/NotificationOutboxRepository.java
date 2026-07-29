package love.takeaway.notification;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface NotificationOutboxRepository extends JpaRepository<NotificationOutbox, Long> {
    List<NotificationOutbox> findByStatusAndNextAttemptAtLessThanEqualOrderByIdAsc(
            NotificationStatus status,
            Instant now,
            Pageable pageable
    );

    List<NotificationOutbox> findTop20ByOrderByIdDesc();
}

