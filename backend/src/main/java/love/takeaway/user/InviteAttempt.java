package love.takeaway.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "invite_attempt")
public class InviteAttempt {

    @Id
    @Column(length = 128)
    private String openid;

    @Column(nullable = false)
    private int failedCount;

    private Instant lockedUntil;

    @Column(nullable = false)
    private Instant updatedAt;

    protected InviteAttempt() {
    }

    public InviteAttempt(String openid) {
        this.openid = openid;
        this.failedCount = 0;
        this.updatedAt = Instant.now();
    }

    public void fail() {
        failedCount++;
        updatedAt = Instant.now();
        if (failedCount >= 5) {
            lockedUntil = updatedAt.plusSeconds(15 * 60L);
            failedCount = 0;
        }
    }

    public void reset() {
        failedCount = 0;
        lockedUntil = null;
        updatedAt = Instant.now();
    }

    public boolean isLocked(Instant now) {
        return lockedUntil != null && lockedUntil.isAfter(now);
    }

    public Instant getLockedUntil() {
        return lockedUntil;
    }
}

