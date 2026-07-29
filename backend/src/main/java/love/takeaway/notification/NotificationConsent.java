package love.takeaway.notification;

import jakarta.persistence.*;
import love.takeaway.user.UserAccount;

import java.time.Instant;

@Entity
@Table(name = "notification_consent")
public class NotificationConsent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private NotificationType notificationType;

    @Column(nullable = false)
    private int grantedCount;

    @Column(nullable = false)
    private int sentCount;

    @Column(length = 32)
    private String lastResult;

    @Column(nullable = false)
    private Instant updatedAt;

    @Version
    private long version;

    protected NotificationConsent() {
    }

    public NotificationConsent(UserAccount user, NotificationType notificationType) {
        this.user = user;
        this.notificationType = notificationType;
        this.updatedAt = Instant.now();
    }

    public void record(String result) {
        lastResult = result;
        if ("accept".equalsIgnoreCase(result)) {
            grantedCount++;
        }
        updatedAt = Instant.now();
    }

    public void markSent() {
        sentCount++;
        updatedAt = Instant.now();
    }

    public int estimatedRemaining() {
        return Math.max(0, grantedCount - sentCount);
    }

    public NotificationType getNotificationType() {
        return notificationType;
    }

    public int getGrantedCount() {
        return grantedCount;
    }

    public int getSentCount() {
        return sentCount;
    }

    public String getLastResult() {
        return lastResult;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}

