package love.takeaway.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InvitationCodeRepository extends JpaRepository<InvitationCode, Long> {
    List<InvitationCode> findByStatus(InvitationStatus status);

    boolean existsByCodeLabel(String codeLabel);
}

