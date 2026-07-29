package love.takeaway.notification;

import com.fasterxml.jackson.databind.JsonNode;
import love.takeaway.config.AppProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Instant;

@Service
@ConditionalOnProperty(prefix = "app.notification", name = "mode", havingValue = "wechat")
public class WechatAccessTokenService {

    private final AppProperties properties;
    private final RestClient restClient;
    private volatile CachedToken cachedToken;

    public WechatAccessTokenService(AppProperties properties, RestClient.Builder restClientBuilder) {
        this.properties = properties;
        this.restClient = restClientBuilder.baseUrl("https://api.weixin.qq.com").build();
    }

    public synchronized String getToken() {
        if (cachedToken != null && cachedToken.expiresAt().isAfter(Instant.now().plusSeconds(60))) {
            return cachedToken.value();
        }
        JsonNode response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/cgi-bin/token")
                        .queryParam("grant_type", "client_credential")
                        .queryParam("appid", properties.getWechat().getAppId())
                        .queryParam("secret", properties.getWechat().getAppSecret())
                        .build())
                .retrieve()
                .body(JsonNode.class);
        if (response == null || !response.hasNonNull("access_token")) {
            throw new IllegalStateException("Unable to obtain WeChat access token: " + response);
        }
        int expiresIn = response.path("expires_in").asInt(7200);
        cachedToken = new CachedToken(response.get("access_token").asText(), Instant.now().plusSeconds(expiresIn));
        return cachedToken.value();
    }

    public synchronized void invalidate() {
        cachedToken = null;
    }

    private record CachedToken(String value, Instant expiresAt) {
    }
}

