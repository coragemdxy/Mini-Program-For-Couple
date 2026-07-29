package love.takeaway.notification;

import jakarta.persistence.*;
import love.takeaway.user.UserAccount;

import java.time.Instant;

@Entity
@Table(name = "notification_outbox")
public class NotificationOutbox {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "receiver_user_id", nullable = false)
    private UserAccount receiver;

    @Column(nullable = false, length = 128)
    private String receiverOpenid;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private NotificationType notificationType;

    @Column(nullable = false, length = 8000)
    private String payloadJson;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private NotificationStatus status;

    @Column(nullable = false)
    private int retryCount;

    @Column(nullable = false)
    private Instant nextAttemptAt;

    @Column(length = 500)
    private String lastError;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant sentAt;

    @Version
    private long version;

    protected NotificationOutbox() {
    }

    public NotificationOutbox(
            UserAccount receiver,
            NotificationType notificationType,
            String payloadJson
    ) {
        this.receiver = receiver;
        this.receiverOpenid = receiver.getOpenid();
        this.notificationType = notificationType;
        this.payloadJson = payloadJson;
        this.status = NotificationStatus.PENDING;
        this.retryCount = 0;
        this.nextAttemptAt = Instant.now();
        this.createdAt = Instant.now();
    }

    public void markSent() {
        status = NotificationStatus.SENT;
        sentAt = Instant.now();
        lastError = null;
    }

    public void markNoPermission(String error) {
        status = NotificationStatus.NO_PERMISSION;
        lastError = truncate(error);
    }

    public void markFailed(String error, boolean retryable) {
        retryCount++;
        lastError = truncate(error);
        if (retryable && retryCount < 5) {
            status = NotificationStatus.PENDING;
            long delaySeconds = Math.min(15L * (1L << retryCount), 15L * 60L);
            nextAttemptAt = Instant.now().plusSeconds(delaySeconds);
        } else {
            status = NotificationStatus.FAILED;
        }
    }

    private String truncate(String value) {
        if (value == null) {
            return null;
        }
        return value.length() <= 500 ? value : value.substring(0, 500);
    }

    public Long getId() {
        return id;
    }

    public UserAccount getReceiver() {
        return receiver;
    }

    public String getReceiverOpenid() {
        return receiverOpenid;
    }

    public NotificationType getNotificationType() {
        return notificationType;
    }

    public String getPayloadJson() {
        return payloadJson;
    }

    public NotificationStatus getStatus() {
        return status;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getSentAt() {
        return sentAt;
    }

    public String getLastError() {
        return lastError;
    }
}
