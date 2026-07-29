package love.takeaway.user;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class InvitationManagementService {

    private static final char[] ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();

    private final InvitationCodeRepository invitations;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom random = new SecureRandom();

    public InvitationManagementService(
            InvitationCodeRepository invitations,
            PasswordEncoder passwordEncoder
    ) {
        this.invitations = invitations;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public InvitationResponse create(CreateInvitationRequest request) {
        String code = randomCode();
        String label = "customer-" + Instant.now().toEpochMilli();
        InvitationCode invitation = invitations.save(new InvitationCode(
                passwordEncoder.encode(code),
                label,
                UserRole.CUSTOMER,
                request.maxUses(),
                Instant.now().plus(request.validDays(), ChronoUnit.DAYS)
        ));
        return new InvitationResponse(
                invitation.getId(),
                code,
                invitation.getTargetRole(),
                invitation.getMaxUses(),
                invitation.getExpiresAt()
        );
    }

    private String randomCode() {
        StringBuilder value = new StringBuilder(14);
        for (int i = 0; i < 12; i++) {
            if (i == 4 || i == 8) {
                value.append('-');
            }
            value.append(ALPHABET[random.nextInt(ALPHABET.length)]);
        }
        return value.toString();
    }

    public record CreateInvitationRequest(
            @jakarta.validation.constraints.Min(1)
            @jakarta.validation.constraints.Max(20)
            int maxUses,
            @jakarta.validation.constraints.Min(1)
            @jakarta.validation.constraints.Max(365)
            int validDays
    ) {
    }

    public record InvitationResponse(
            Long id,
            String code,
            UserRole role,
            int maxUses,
            Instant expiresAt
    ) {
    }
}
