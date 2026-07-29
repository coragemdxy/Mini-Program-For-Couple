package love.takeaway.config;

import love.takeaway.user.InvitationCode;
import love.takeaway.user.InvitationCodeRepository;
import love.takeaway.user.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
public class BootstrapInvitationData implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(BootstrapInvitationData.class);
    private static final String LABEL = "bootstrap-merchant";

    private final AppProperties properties;
    private final InvitationCodeRepository invitations;
    private final PasswordEncoder passwordEncoder;

    public BootstrapInvitationData(
            AppProperties properties,
            InvitationCodeRepository invitations,
            PasswordEncoder passwordEncoder
    ) {
        this.properties = properties;
        this.invitations = invitations;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        String code = properties.getBootstrap().getMerchantCode();
        if (code == null || code.isBlank() || invitations.existsByCodeLabel(LABEL)) {
            return;
        }
        int validDays = Math.max(1, Math.min(properties.getBootstrap().getValidDays(), 30));
        invitations.save(new InvitationCode(
                passwordEncoder.encode(code.trim()),
                LABEL,
                UserRole.MERCHANT,
                1,
                Instant.now().plus(validDays, ChronoUnit.DAYS)
        ));
        log.info("One-time bootstrap merchant invitation created; plaintext code is not logged");
    }
}
