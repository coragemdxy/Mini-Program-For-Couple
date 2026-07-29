package love.takeaway.notification;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import love.takeaway.config.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
@ConditionalOnProperty(prefix = "app.notification", name = "mode", havingValue = "wechat")
public class WechatNotificationSender implements NotificationMessageSender {

    private static final Logger log = LoggerFactory.getLogger(WechatNotificationSender.class);

    private final AppProperties properties;
    private final WechatAccessTokenService tokens;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public WechatNotificationSender(
            AppProperties properties,
            WechatAccessTokenService tokens,
            ObjectMapper objectMapper,
            RestClient.Builder restClientBuilder
    ) {
        this.properties = properties;
        this.tokens = tokens;
        this.objectMapper = objectMapper;
        this.restClient = restClientBuilder.baseUrl("https://api.weixin.qq.com").build();
    }

    @Override
    public SendResult send(NotificationOutbox message) {
        String templateId = templateId(message.getNotificationType());
        if (templateId == null || templateId.isBlank()) {
            return SendResult.fail("WeChat template ID is not configured");
        }
        try {
            Map<String, Object> payload = objectMapper.readValue(message.getPayloadJson(), Map.class);
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("touser", message.getReceiverOpenid());
            body.put("template_id", templateId);
            body.put("page", payload.getOrDefault("page", "pages/launch/index"));
            body.put("miniprogram_state", "formal");
            body.put("lang", "zh_CN");
            body.put("data", payload.get("data"));

            JsonNode result = send(body, tokens.getToken());
            int errorCode = result == null ? -1 : result.path("errcode").asInt(-1);
            if (errorCode == 0) {
                return SendResult.delivered();
            }
            String error = "WeChat errcode=" + errorCode + ", errmsg="
                    + (result == null ? "empty response" : result.path("errmsg").asText());
            if (errorCode == 43101) {
                return SendResult.noPermission(error);
            }
            if (errorCode == 40001 || errorCode == 40014 || errorCode == 42001) {
                tokens.invalidate();
                JsonNode retryResult = send(body, tokens.getToken());
                if (retryResult != null && retryResult.path("errcode").asInt(-1) == 0) {
                    return SendResult.delivered();
                }
                return SendResult.retry(error);
            }
            return SendResult.fail(error);
        } catch (Exception exception) {
            log.warn("WeChat notification send failed", exception);
            return SendResult.retry(exception.getMessage());
        }
    }

    private JsonNode send(Map<String, Object> body, String accessToken) {
        return restClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/cgi-bin/message/subscribe/send")
                        .queryParam("access_token", accessToken)
                        .build())
                .body(body)
                .retrieve()
                .body(JsonNode.class);
    }

    private String templateId(NotificationType type) {
        return type == NotificationType.NEW_ORDER
                ? properties.getWechat().getNewOrderTemplateId()
                : properties.getWechat().getOrderStatusTemplateId();
    }
}
