package love.takeaway.user;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "wx_user")
public class UserAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 128)
    private String openid;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private UserRole role;

    @Column(nullable = false, length = 80)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private UserStatus status;

    @Column(nullable = false)
    private Instant activatedAt;

    @Column(nullable = false)
    private Instant lastLoginAt;

    @Column(nullable = false)
    private Instant createdAt;

    @Version
    private long version;

    protected UserAccount() {
    }

    public UserAccount(String openid, UserRole role, String nickname) {
        Instant now = Instant.now();
        this.openid = openid;
        this.role = role;
        this.nickname = nickname;
        this.status = UserStatus.ACTIVE;
        this.activatedAt = now;
        this.lastLoginAt = now;
        this.createdAt = now;
    }

    public void recordLogin() {
        this.lastLoginAt = Instant.now();
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void block() {
        this.status = UserStatus.BLOCKED;
    }

    public Long getId() {
        return id;
    }

    public String getOpenid() {
        return openid;
    }

    public UserRole getRole() {
        return role;
    }

    public String getNickname() {
        return nickname;
    }

    public UserStatus getStatus() {
        return status;
    }

    public Instant getActivatedAt() {
        return activatedAt;
    }

    public Instant getLastLoginAt() {
        return lastLoginAt;
    }
}

