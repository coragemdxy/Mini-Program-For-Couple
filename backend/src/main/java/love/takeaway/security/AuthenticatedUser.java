package love.takeaway.security;

import love.takeaway.common.ApiException;
import love.takeaway.user.UserRole;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthenticatedUser {

    public AppPrincipal require() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AppPrincipal principal)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "AUTHENTICATION_REQUIRED", "请先完成微信身份绑定");
        }
        return principal;
    }

    public AppPrincipal requireRole(UserRole role) {
        AppPrincipal principal = require();
        if (principal.role() != role) {
            throw ApiException.forbidden("ROLE_REQUIRED", "当前账号角色不允许执行此操作");
        }
        return principal;
    }
}

