package love.takeaway.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "app.notification", name = "mode", havingValue = "log", matchIfMissing = true)
public class LoggingNotificationSender implements NotificationMessageSender {

    private static final Logger log = LoggerFactory.getLogger(LoggingNotificationSender.class);

    @Override
    public SendResult send(NotificationOutbox message) {
        log.info("LOCAL WECHAT NOTICE type={} receiver={} payload={}",
                message.getNotificationType(), mask(message.getReceiverOpenid()), message.getPayloadJson());
        return SendResult.delivered();
    }

    private String mask(String openid) {
        if (openid == null || openid.length() < 8) {
            return "***";
        }
        return openid.substring(0, 4) + "***" + openid.substring(openid.length() - 4);
    }
}
