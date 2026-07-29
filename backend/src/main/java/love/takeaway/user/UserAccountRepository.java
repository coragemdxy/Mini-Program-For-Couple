package love.takeaway.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
    Optional<UserAccount> findByOpenid(String openid);

    List<UserAccount> findByRoleAndStatus(UserRole role, UserStatus status);
}

