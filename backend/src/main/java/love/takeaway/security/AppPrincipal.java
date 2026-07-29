package love.takeaway.security;

import love.takeaway.user.UserRole;

public record AppPrincipal(
        Long userId,
        String openid,
        UserRole role,
        String nickname
) {
}

