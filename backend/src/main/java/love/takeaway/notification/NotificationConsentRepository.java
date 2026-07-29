package love.takeaway.notification;

import love.takeaway.user.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationConsentRepository extends JpaRepository<NotificationConsent, Long> {
    Optional<NotificationConsent> findByUserAndNotificationType(UserAccount user, NotificationType type);

    List<NotificationConsent> findByUser(UserAccount user);
}

