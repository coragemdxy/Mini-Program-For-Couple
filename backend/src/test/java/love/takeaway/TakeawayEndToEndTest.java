package love.takeaway;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import love.takeaway.config.LocalSeedData;
import love.takeaway.notification.*;
import love.takeaway.user.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TakeawayEndToEndTest {

    private static final String CUSTOMER_OPENID = "test-customer-openid";
    private static final String MERCHANT_OPENID = "test-merchant-openid";

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    InvitationCodeRepository invitations;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    NotificationDispatcher dispatcher;

    @Autowired
    NotificationOutboxRepository outbox;

    @Test
    @Transactional
    void completeCustomerAndMerchantWorkflow() throws Exception {
        seedInvitations();

        mockMvc.perform(get("/api/session").header("X-Debug-Openid", CUSTOMER_OPENID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("NEED_INVITE"));

        redeem(CUSTOMER_OPENID, LocalSeedData.CUSTOMER_CODE, "小可爱", "CUSTOMER");
        redeem(MERCHANT_OPENID, LocalSeedData.MERCHANT_CODE, "专属店长", "MERCHANT");

        mockMvc.perform(post("/api/merchant/invitations")
                        .header("X-Debug-Openid", MERCHANT_OPENID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"maxUses":1,"validDays":30}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("CUSTOMER"))
                .andExpect(jsonPath("$.code").isNotEmpty());

        mockMvc.perform(post("/api/merchant/categories")
                        .header("X-Debug-Openid", CUSTOMER_OPENID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"不允许","sortOrder":1,"enabled":true}
                                """))
                .andExpect(status().isForbidden());

        JsonNode category = json(mockMvc.perform(post("/api/merchant/categories")
                        .header("X-Debug-Openid", MERCHANT_OPENID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"测试饮品","iconUrl":null,"sortOrder":10,"enabled":true}
                                """))
                .andExpect(status().isCreated())
                .andReturn());
        long categoryId = category.get("id").asLong();

        JsonNode product = json(mockMvc.perform(post("/api/merchant/products")
                        .header("X-Debug-Openid", MERCHANT_OPENID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "categoryId": %d,
                                  "name": "测试奶茶",
                                  "description": "端到端测试商品",
                                  "coverUrl": null,
                                  "virtualPrice": 12.00,
                                  "stock": 5,
                                  "enabled": true,
                                  "recommended": true,
                                  "sortOrder": 10,
                                  "searchKeywords": "奶茶 测试",
                                  "optionGroups": [{
                                    "name": "温度",
                                    "required": true,
                                    "minSelect": 1,
                                    "maxSelect": 1,
                                    "sortOrder": 10,
                                    "options": [
                                      {"name":"热","extraPrice":0,"enabled":true,"sortOrder":10},
                                      {"name":"少冰","extraPrice":1,"enabled":true,"sortOrder":20}
                                    ]
                                  }]
                                }
                                """.formatted(categoryId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("测试奶茶"))
                .andReturn());
        long productId = product.get("id").asLong();
        long optionId = product.at("/optionGroups/0/options/1/id").asLong();

        mockMvc.perform(get("/api/products")
                        .header("X-Debug-Openid", CUSTOMER_OPENID)
                        .param("query", "奶茶"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));

        mockMvc.perform(post("/api/notifications/consents")
                        .header("X-Debug-Openid", MERCHANT_OPENID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"type":"NEW_ORDER","result":"accept"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estimatedRemaining").value(1));

        String createOrder = """
                {
                  "idempotencyKey":"test-order-idempotency-0001",
                  "remark":"半小时后想要",
                  "items":[{"productId":%d,"quantity":2,"optionIds":[%d]}]
                }
                """.formatted(productId, optionId);
        JsonNode order = json(mockMvc.perform(post("/api/orders")
                        .header("X-Debug-Openid", CUSTOMER_OPENID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createOrder))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SUBMITTED"))
                .andExpect(jsonPath("$.totalVirtualPrice").value(26.0))
                .andReturn());
        long orderId = order.get("id").asLong();

        JsonNode duplicate = json(mockMvc.perform(post("/api/orders")
                        .header("X-Debug-Openid", CUSTOMER_OPENID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createOrder))
                .andExpect(status().isCreated())
                .andReturn());
        assertThat(duplicate.get("id").asLong()).isEqualTo(orderId);

        mockMvc.perform(get("/api/merchant/orders")
                        .header("X-Debug-Openid", MERCHANT_OPENID)
                        .param("status", "SUBMITTED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));

        changeStatus(orderId, "ACCEPTED", "马上安排");
        changeStatus(orderId, "PREPARING", "正在准备");
        changeStatus(orderId, "COMPLETED", "已经准备好啦");

        mockMvc.perform(get("/api/orders/{id}", orderId)
                        .header("X-Debug-Openid", CUSTOMER_OPENID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.events.length()").value(4));

        mockMvc.perform(post("/api/orders/{id}/cancel", orderId)
                        .header("X-Debug-Openid", CUSTOMER_OPENID))
                .andExpect(status().isConflict());

        MockMultipartFile image = new MockMultipartFile(
                "file",
                "tiny.png",
                MediaType.IMAGE_PNG_VALUE,
                "not-a-real-image-but-valid-upload-fixture".getBytes(StandardCharsets.UTF_8)
        );
        JsonNode media = json(mockMvc.perform(multipart("/api/merchant/media")
                        .file(image)
                        .header("X-Debug-Openid", MERCHANT_OPENID))
                .andExpect(status().isCreated())
                .andReturn());
        mockMvc.perform(get(media.get("url").asText()))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", MediaType.IMAGE_PNG_VALUE));

        dispatcher.dispatch();
        assertThat(outbox.findTop20ByOrderByIdDesc())
                .isNotEmpty()
                .allMatch(message -> message.getStatus() == NotificationStatus.SENT);
    }

    private void seedInvitations() {
        Instant expiry = Instant.now().plus(1, ChronoUnit.DAYS);
        invitations.save(new InvitationCode(
                passwordEncoder.encode(LocalSeedData.CUSTOMER_CODE),
                "test-customer",
                UserRole.CUSTOMER,
                1,
                expiry
        ));
        invitations.save(new InvitationCode(
                passwordEncoder.encode(LocalSeedData.MERCHANT_CODE),
                "test-merchant",
                UserRole.MERCHANT,
                1,
                expiry
        ));
    }

    private void redeem(String openid, String code, String nickname, String role) throws Exception {
        mockMvc.perform(post("/api/invitations/redeem")
                        .header("X-Debug-Openid", openid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"%s","nickname":"%s"}
                                """.formatted(code, nickname)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("ACTIVE"))
                .andExpect(jsonPath("$.role").value(role));
    }

    private void changeStatus(long orderId, String status, String message) throws Exception {
        mockMvc.perform(patch("/api/merchant/orders/{id}/status", orderId)
                        .header("X-Debug-Openid", MERCHANT_OPENID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"%s","message":"%s"}
                                """.formatted(status, message)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(status));
    }

    private JsonNode json(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsByteArray());
    }
}
