package love.takeaway.security;

import jakarta.servlet.http.HttpServletRequest;
import love.takeaway.common.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class CurrentIdentity {

    public static final String OPENID_ATTRIBUTE = CurrentIdentity.class.getName() + ".openid";

    private final HttpServletRequest request;

    public CurrentIdentity(HttpServletRequest request) {
        this.request = request;
    }

    public String requireOpenid() {
        Object value = request.getAttribute(OPENID_ATTRIBUTE);
        if (!(value instanceof String openid) || openid.isBlank()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "WECHAT_IDENTITY_MISSING",
                    "没有获取到微信身份，请从微信小程序重新进入");
        }
        return openid;
    }
}

