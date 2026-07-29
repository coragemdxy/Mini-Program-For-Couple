package love.takeaway.config;

import love.takeaway.catalog.*;
import love.takeaway.user.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@ConditionalOnProperty(prefix = "app.local-seed", name = "enabled", havingValue = "true")
public class LocalSeedData implements ApplicationRunner {

    public static final String CUSTOMER_CODE = "LOVE-CUSTOMER";
    public static final String MERCHANT_CODE = "LOVE-MERCHANT";

    private static final Logger log = LoggerFactory.getLogger(LocalSeedData.class);

    private final InvitationCodeRepository invitations;
    private final CategoryRepository categories;
    private final ProductRepository products;
    private final PasswordEncoder passwordEncoder;

    public LocalSeedData(
            InvitationCodeRepository invitations,
            CategoryRepository categories,
            ProductRepository products,
            PasswordEncoder passwordEncoder
    ) {
        this.invitations = invitations;
        this.categories = categories;
        this.products = products;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedInvitations();
        seedCatalog();
        log.info("Local identities: customer openid=local-customer, merchant openid=local-merchant");
        log.info("Local invitation codes: customer={}, merchant={}", CUSTOMER_CODE, MERCHANT_CODE);
    }

    private void seedInvitations() {
        Instant expiry = Instant.now().plus(3650, ChronoUnit.DAYS);
        if (!invitations.existsByCodeLabel("local-customer")) {
            invitations.save(new InvitationCode(
                    passwordEncoder.encode(CUSTOMER_CODE),
                    "local-customer",
                    UserRole.CUSTOMER,
                    1,
                    expiry
            ));
        }
        if (!invitations.existsByCodeLabel("local-merchant")) {
            invitations.save(new InvitationCode(
                    passwordEncoder.encode(MERCHANT_CODE),
                    "local-merchant",
                    UserRole.MERCHANT,
                    1,
                    expiry
            ));
        }
    }

    private void seedCatalog() {
        if (categories.count() > 0) {
            return;
        }

        Category drinks = categories.save(new Category("甜甜饮品", null, 10, true));
        Category snacks = categories.save(new Category("快乐零食", null, 20, true));
        Category services = categories.save(new Category("专属服务", null, 30, true));

        Product milkTea = new Product(
                drinks,
                "专属奶茶",
                "今天也要喝一杯甜甜的奶茶。",
                null,
                new BigDecimal("12.00"),
                null,
                true,
                true,
                10,
                "奶茶 饮料 甜"
        );
        ProductOptionGroup temperature = new ProductOptionGroup("温度", true, 1, 1, 10);
        temperature.addOption("热", BigDecimal.ZERO, true, 10);
        temperature.addOption("少冰", BigDecimal.ZERO, true, 20);
        temperature.addOption("正常冰", BigDecimal.ZERO, true, 30);
        ProductOptionGroup sweetness = new ProductOptionGroup("甜度", true, 1, 1, 20);
        sweetness.addOption("无糖", BigDecimal.ZERO, true, 10);
        sweetness.addOption("半糖", BigDecimal.ZERO, true, 20);
        sweetness.addOption("全糖", BigDecimal.ZERO, true, 30);
        milkTea.replaceOptionGroups(List.of(temperature, sweetness));
        products.save(milkTea);

        products.save(new Product(
                snacks,
                "薯片补给",
                "适合窝在沙发上看电影。",
                null,
                new BigDecimal("8.00"),
                10,
                true,
                true,
                10,
                "薯片 零食 电影"
        ));

        products.save(new Product(
                services,
                "抱抱券",
                "下单后可兑换一个超长抱抱。",
                null,
                new BigDecimal("1.00"),
                null,
                true,
                true,
                10,
                "抱抱 礼物 服务"
        ));

        products.save(new Product(
                services,
                "陪散步",
                "路线和时间可以写在订单备注里。",
                null,
                new BigDecimal("5.00"),
                null,
                true,
                false,
                20,
                "散步 陪伴 服务"
        ));
    }
}

