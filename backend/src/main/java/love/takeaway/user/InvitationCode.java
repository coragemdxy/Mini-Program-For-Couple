package love.takeaway.user;

import jakarta.persistence.*;
import love.takeaway.common.ApiException;

import java.time.Instant;

@Entity
@Table(name = "invitation_code")
public class InvitationCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String codeHash;

    @Column(nullable = false, length = 80)
    private String codeLabel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private UserRole targetRole;

    @Column(nullable = false)
    private int maxUses;

    @Column(nullable = false)
    private int usedCount;

    @Column(nullable = false)
    private Instant expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private InvitationStatus status;

    @Column(nullable = false)
    private Instant createdAt;

    @Version
    private long version;

    protected InvitationCode() {
    }

    public InvitationCode(
            String codeHash,
            String codeLabel,
            UserRole targetRole,
            int maxUses,
            Instant expiresAt
    ) {
        this.codeHash = codeHash;
        this.codeLabel = codeLabel;
        this.targetRole = targetRole;
        this.maxUses = maxUses;
        this.usedCount = 0;
        this.expiresAt = expiresAt;
        this.status = InvitationStatus.ACTIVE;
        this.createdAt = Instant.now();
    }

    public boolean canUse(Instant now) {
        return status == InvitationStatus.ACTIVE && usedCount < maxUses && expiresAt.isAfter(now);
    }

    public void consume() {
        if (!canUse(Instant.now())) {
            throw ApiException.conflict("INVITATION_UNAVAILABLE", "邀请码已失效或已被使用");
        }
        usedCount++;
        if (usedCount >= maxUses) {
            status = InvitationStatus.EXHAUSTED;
        }
    }

    public String getCodeHash() {
        return codeHash;
    }

    public Long getId() {
        return id;
    }

    public String getCodeLabel() {
        return codeLabel;
    }

    public UserRole getTargetRole() {
        return targetRole;
    }

    public InvitationStatus getStatus() {
        return status;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public int getMaxUses() {
        return maxUses;
    }

    public int getUsedCount() {
        return usedCount;
    }
}
