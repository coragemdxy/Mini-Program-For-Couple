package love.takeaway.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import love.takeaway.config.AppProperties;
import love.takeaway.user.UserAccount;
import love.takeaway.user.UserAccountRepository;
import love.takeaway.user.UserStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

@Component
public class IdentityHeaderFilter extends OncePerRequestFilter {

    private final AppProperties properties;
    private final UserAccountRepository users;

    public IdentityHeaderFilter(AppProperties properties, UserAccountRepository users) {
        this.properties = properties;
        this.users = users;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String headerName = resolveHeaderName();
        String openid = normalize(request.getHeader(headerName));

        if (openid != null) {
            request.setAttribute(CurrentIdentity.OPENID_ATTRIBUTE, openid);
            users.findByOpenid(openid)
                    .filter(user -> user.getStatus() == UserStatus.ACTIVE)
                    .ifPresent(this::authenticate);
        }

        filterChain.doFilter(request, response);
    }

    private String resolveHeaderName() {
        if ("wechat-cloud".equalsIgnoreCase(properties.getIdentity().getMode())) {
            return properties.getIdentity().getWechatHeader();
        }
        return properties.getIdentity().getDebugHeader();
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.length() > 128) {
            return null;
        }
        return trimmed;
    }

    private void authenticate(UserAccount user) {
        AppPrincipal principal = new AppPrincipal(user.getId(), user.getOpenid(), user.getRole(), user.getNickname());
        List<SimpleGrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name().toUpperCase(Locale.ROOT))
        );
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(principal, null, authorities)
        );
    }
}
