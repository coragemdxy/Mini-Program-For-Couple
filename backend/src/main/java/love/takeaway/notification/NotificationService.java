package love.takeaway.notification;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import love.takeaway.common.ApiException;
import love.takeaway.security.AppPrincipal;
import love.takeaway.security.AuthenticatedUser;
import love.takeaway.user.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class NotificationService {

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.of("Asia/Shanghai"));

    private final NotificationOutboxRepository outbox;
    private final NotificationConsentRepository consents;
    private final UserAccountRepository users;
    private final AuthenticatedUser authenticatedUser;
    private final ObjectMapper objectMapper;

    public NotificationService(
            NotificationOutboxRepository outbox,
            NotificationConsentRepository consents,
            UserAccountRepository users,
            AuthenticatedUser authenticatedUser,
            ObjectMapper objectMapper
    ) {
        this.outbox = outbox;
        this.consents = consents;
        this.users = users;
        this.authenticatedUser = authenticatedUser;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void enqueueNewOrder(
            Long orderId,
            String orderNo,
            String customerName,
            String itemSummary,
            Instant createdAt
    ) {
        Map<String, Object> payload = Map.of(
                "page", "pages/merchant/order-detail/index?id=" + orderId,
                "data", Map.of(
                        "character_string1", value(orderNo),
                        "thing2", value(customerName),
                        "thing3", value(truncate(itemSummary, 20)),
                        "time4", value(TIME_FORMAT.format(createdAt)),
                        "phrase5", value("等待处理")
                )
        );
        users.findByRoleAndStatus(UserRole.MERCHANT, UserStatus.ACTIVE)
                .forEach(merchant -> outbox.save(new NotificationOutbox(
                        merchant,
                        NotificationType.NEW_ORDER,
                        json(payload)
                )));
    }

    @Transactional
    public void enqueueOrderStatus(
            UserAccount customer,
            Long orderId,
            String orderNo,
            String statusLabel,
            String message
    ) {
        Map<String, Object> payload = Map.of(
                "page", "pages/customer/order-detail/index?id=" + orderId,
                "data", Map.of(
                        "character_string1", value(orderNo),
                        "phrase2", value(statusLabel),
                        "thing3", value(truncate(message == null ? "订单状态已更新" : message, 20)),
                        "time4", value(TIME_FORMAT.format(Instant.now()))
                )
        );
        outbox.save(new NotificationOutbox(customer, NotificationType.ORDER_STATUS, json(payload)));
    }

    @Transactional
    public ConsentResponse recordConsent(ConsentRequest request) {
        AppPrincipal principal = authenticatedUser.require();
        UserAccount user = users.findById(principal.userId())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "USER_NOT_FOUND", "用户不存在"));
        NotificationConsent consent = consents.findByUserAndNotificationType(user, request.type())
                .orElseGet(() -> new NotificationConsent(user, request.type()));
        consent.record(request.result());
        return toResponse(consents.save(consent));
    }

    @Transactional(readOnly = true)
    public List<ConsentResponse> settings() {
        AppPrincipal principal = authenticatedUser.require();
        UserAccount user = users.findById(principal.userId())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "USER_NOT_FOUND", "用户不存在"));
        Map<NotificationType, NotificationConsent> byType = new EnumMap<>(NotificationType.class);
        consents.findByUser(user).forEach(consent -> byType.put(consent.getNotificationType(), consent));
        return Arrays.stream(NotificationType.values())
                .map(type -> {
                    NotificationConsent consent = byType.get(type);
                    return consent == null
                            ? new ConsentResponse(type, 0, 0, 0, null, null)
                            : toResponse(consent);
                })
                .toList();
    }

    @Transactional
    public void markDelivered(UserAccount user, NotificationType type) {
        consents.findByUserAndNotificationType(user, type).ifPresent(NotificationConsent::markSent);
    }

    private ConsentResponse toResponse(NotificationConsent consent) {
        return new ConsentResponse(
                consent.getNotificationType(),
                consent.getGrantedCount(),
                consent.getSentCount(),
                consent.estimatedRemaining(),
                consent.getLastResult(),
                consent.getUpdatedAt()
        );
    }

    private Map<String, String> value(String value) {
        return Map.of("value", value == null ? "" : value);
    }

    private String json(Map<String, Object> value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to serialize notification", exception);
        }
    }

    private String truncate(String value, int maxLength) {
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

    public record ConsentRequest(NotificationType type, String result) {
    }

    public record ConsentResponse(
            NotificationType type,
            int grantedCount,
            int sentCount,
            int estimatedRemaining,
            String lastResult,
            Instant updatedAt
    ) {
    }
}

