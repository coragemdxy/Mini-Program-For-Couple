package love.takeaway.user;

import love.takeaway.common.ApiException;
import love.takeaway.security.CurrentIdentity;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static love.takeaway.user.SessionDtos.*;

@Service
public class SessionService {

    private final CurrentIdentity currentIdentity;
    private final UserAccountRepository users;
    private final InvitationCodeRepository invitations;
    private final InviteAttemptRepository attempts;
    private final PasswordEncoder passwordEncoder;

    public SessionService(
            CurrentIdentity currentIdentity,
            UserAccountRepository users,
            InvitationCodeRepository invitations,
            InviteAttemptRepository attempts,
            PasswordEncoder passwordEncoder
    ) {
        this.currentIdentity = currentIdentity;
        this.users = users;
        this.invitations = invitations;
        this.attempts = attempts;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public SessionResponse getSession() {
        String openid = currentIdentity.requireOpenid();
        return users.findByOpenid(openid)
                .map(user -> new SessionResponse(
                        user.getStatus() == UserStatus.ACTIVE ? "ACTIVE" : "BLOCKED",
                        user.getId(),
                        user.getRole(),
                        user.getNickname(),
                        mask(openid)
                ))
                .orElseGet(() -> new SessionResponse("NEED_INVITE", null, null, null, mask(openid)));
    }

    @Transactional
    public SessionResponse redeem(RedeemInvitationRequest request) {
        String openid = currentIdentity.requireOpenid();
        UserAccount existing = users.findByOpenid(openid).orElse(null);
        if (existing != null) {
            return new SessionResponse(
                    existing.getStatus() == UserStatus.ACTIVE ? "ACTIVE" : "BLOCKED",
                    existing.getId(),
                    existing.getRole(),
                    existing.getNickname(),
                    mask(openid)
            );
        }

        InviteAttempt attempt = attempts.findById(openid).orElseGet(() -> new InviteAttempt(openid));
        if (attempt.isLocked(Instant.now())) {
            throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "INVITATION_LOCKED",
                    "邀请码尝试次数过多，请稍后再试");
        }

        InvitationCode invitation = invitations.findByStatus(InvitationStatus.ACTIVE).stream()
                .filter(candidate -> candidate.canUse(Instant.now()))
                .filter(candidate -> passwordEncoder.matches(request.code().trim(), candidate.getCodeHash()))
                .findFirst()
                .orElse(null);

        if (invitation == null) {
            attempt.fail();
            attempts.save(attempt);
            throw ApiException.badRequest("INVITATION_INVALID", "邀请码错误、已过期或已被使用");
        }

        invitation.consume();
        attempt.reset();
        invitations.save(invitation);
        attempts.save(attempt);

        UserAccount user = users.save(new UserAccount(
                openid,
                invitation.getTargetRole(),
                request.nickname().trim()
        ));
        return new SessionResponse("ACTIVE", user.getId(), user.getRole(), user.getNickname(), mask(openid));
    }

    private String mask(String openid) {
        if (openid.length() <= 8) {
            return "***";
        }
        return openid.substring(0, 4) + "***" + openid.substring(openid.length() - 4);
    }
}

