package love.takeaway.notification;

import love.takeaway.config.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Component
public class NotificationDispatcher {

    private static final Logger log = LoggerFactory.getLogger(NotificationDispatcher.class);

    private final NotificationOutboxRepository outbox;
    private final NotificationMessageSender sender;
    private final NotificationService notificationService;
    private final AppProperties properties;

    public NotificationDispatcher(
            NotificationOutboxRepository outbox,
            NotificationMessageSender sender,
            NotificationService notificationService,
            AppProperties properties
    ) {
        this.outbox = outbox;
        this.sender = sender;
        this.notificationService = notificationService;
        this.properties = properties;
    }

    @Scheduled(fixedDelayString = "${app.notification.dispatch-delay-ms:10000}")
    @Transactional
    public void dispatch() {
        List<NotificationOutbox> due = outbox
                .findByStatusAndNextAttemptAtLessThanEqualOrderByIdAsc(
                        NotificationStatus.PENDING,
                        Instant.now(),
                        PageRequest.of(0, properties.getNotification().getBatchSize())
                );
        for (NotificationOutbox message : due) {
            try {
                NotificationMessageSender.SendResult result = sender.send(message);
                if (result.success()) {
                    message.markSent();
                    notificationService.markDelivered(message.getReceiver(), message.getNotificationType());
                } else if (result.noPermission()) {
                    message.markNoPermission(result.error());
                } else {
                    message.markFailed(result.error(), result.retryable());
                }
            } catch (Exception exception) {
                log.warn("Notification dispatch failed for outbox {}", message.getId(), exception);
                message.markFailed(exception.getMessage(), true);
            }
        }
    }
}

