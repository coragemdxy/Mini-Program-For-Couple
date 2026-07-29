package love.takeaway.order;

import love.takeaway.catalog.*;
import love.takeaway.common.ApiException;
import love.takeaway.common.PageResponse;
import love.takeaway.notification.NotificationService;
import love.takeaway.security.AppPrincipal;
import love.takeaway.security.AuthenticatedUser;
import love.takeaway.user.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static love.takeaway.order.OrderDtos.*;

@Service
public class OrderService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final DateTimeFormatter ORDER_DATE =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss").withZone(ZoneId.of("Asia/Shanghai"));

    private final CustomerOrderRepository orders;
    private final ProductRepository products;
    private final UserAccountRepository users;
    private final AuthenticatedUser authenticatedUser;
    private final NotificationService notifications;
    private final OrderMapper mapper;

    public OrderService(
            CustomerOrderRepository orders,
            ProductRepository products,
            UserAccountRepository users,
            AuthenticatedUser authenticatedUser,
            NotificationService notifications,
            OrderMapper mapper
    ) {
        this.orders = orders;
        this.products = products;
        this.users = users;
        this.authenticatedUser = authenticatedUser;
        this.notifications = notifications;
        this.mapper = mapper;
    }

    @Transactional
    public OrderResponse create(CreateOrderRequest request) {
        AppPrincipal principal = authenticatedUser.requireRole(UserRole.CUSTOMER);
        UserAccount customer = users.findById(principal.userId())
                .orElseThrow(() -> ApiException.notFound("USER_NOT_FOUND", "用户不存在"));

        CustomerOrder existing = orders
                .findByCustomerIdAndIdempotencyKey(customer.getId(), request.idempotencyKey())
                .orElse(null);
        if (existing != null) {
            return mapper.toResponse(orders.findDetailedById(existing.getId()).orElseThrow(), true);
        }

        List<PreparedLine> lines = request.items().stream().map(this::prepareLine).toList();
        BigDecimal total = lines.stream()
                .map(line -> line.unitPrice().multiply(BigDecimal.valueOf(line.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        CustomerOrder order = new CustomerOrder(
                generateOrderNo(),
                customer,
                total,
                clean(request.remark()),
                request.idempotencyKey()
        );
        lines.forEach(line -> order.addItem(
                line.product().getId(),
                line.product().getName(),
                line.product().getCoverUrl(),
                line.optionSummary(),
                line.unitPrice(),
                line.quantity()
        ));
        order.addEvent(customer, null, OrderStatus.SUBMITTED, "订单已提交");
        CustomerOrder saved = orders.saveAndFlush(order);

        String itemSummary = lines.stream()
                .map(line -> line.product().getName() + "×" + line.quantity())
                .collect(Collectors.joining("、"));
        notifications.enqueueNewOrder(
                saved.getId(),
                saved.getOrderNo(),
                customer.getNickname(),
                itemSummary,
                saved.getCreatedAt()
        );
        return mapper.toResponse(saved, true);
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> myOrders(int page, int size) {
        AppPrincipal principal = authenticatedUser.requireRole(UserRole.CUSTOMER);
        Pageable pageable = PageRequest.of(
                Math.max(0, page),
                Math.min(Math.max(1, size), 50),
                Sort.by(Sort.Order.desc("createdAt"))
        );
        return PageResponse.from(orders.findByCustomerId(principal.userId(), pageable)
                .map(order -> mapper.toResponse(order, false)));
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> merchantOrders(OrderStatus status, int page, int size) {
        authenticatedUser.requireRole(UserRole.MERCHANT);
        Pageable pageable = PageRequest.of(
                Math.max(0, page),
                Math.min(Math.max(1, size), 50),
                Sort.by(Sort.Order.desc("createdAt"))
        );
        Page<CustomerOrder> result = status == null
                ? orders.findAll(pageable)
                : orders.findByStatus(status, pageable);
        return PageResponse.from(result.map(order -> mapper.toResponse(order, false)));
    }

    @Transactional(readOnly = true)
    public OrderResponse detail(Long id) {
        AppPrincipal principal = authenticatedUser.require();
        CustomerOrder order = orders.findDetailedById(id)
                .orElseThrow(() -> ApiException.notFound("ORDER_NOT_FOUND", "订单不存在"));
        if (principal.role() == UserRole.CUSTOMER && !order.getCustomer().getId().equals(principal.userId())) {
            throw ApiException.forbidden("ORDER_ACCESS_DENIED", "不能查看其他用户的订单");
        }
        return mapper.toResponse(order, true);
    }

    @Transactional
    public OrderResponse cancel(Long id) {
        AppPrincipal principal = authenticatedUser.requireRole(UserRole.CUSTOMER);
        UserAccount customer = users.findById(principal.userId())
                .orElseThrow(() -> ApiException.notFound("USER_NOT_FOUND", "用户不存在"));
        CustomerOrder order = orders.findForUpdateById(id)
                .orElseThrow(() -> ApiException.notFound("ORDER_NOT_FOUND", "订单不存在"));
        if (!order.getCustomer().getId().equals(principal.userId())) {
            throw ApiException.forbidden("ORDER_ACCESS_DENIED", "不能取消其他用户的订单");
        }
        order.transition(customer, OrderStatus.CANCELLED, "用户取消订单");
        return mapper.toResponse(order, true);
    }

    @Transactional
    public OrderResponse changeStatus(Long id, ChangeOrderStatusRequest request) {
        AppPrincipal principal = authenticatedUser.requireRole(UserRole.MERCHANT);
        UserAccount merchant = users.findById(principal.userId())
                .orElseThrow(() -> ApiException.notFound("USER_NOT_FOUND", "用户不存在"));
        CustomerOrder order = orders.findForUpdateById(id)
                .orElseThrow(() -> ApiException.notFound("ORDER_NOT_FOUND", "订单不存在"));
        order.transition(merchant, request.status(), clean(request.message()));
        orders.flush();
        notifications.enqueueOrderStatus(
                order.getCustomer(),
                order.getId(),
                order.getOrderNo(),
                request.status().getLabel(),
                request.message()
        );
        return mapper.toResponse(order, true);
    }

    private PreparedLine prepareLine(OrderLineRequest request) {
        Product product = products.findForOrderById(request.productId())
                .orElseThrow(() -> ApiException.notFound("PRODUCT_NOT_FOUND", "商品不存在"));
        if (!product.isEnabled()) {
            throw ApiException.conflict("PRODUCT_UNAVAILABLE", product.getName() + " 已下架或暂时不可用");
        }
        if (!product.getCategory().isEnabled()) {
            throw ApiException.conflict("CATEGORY_UNAVAILABLE", product.getName() + " 所在分类已停用");
        }
        try {
            product.reserveStock(request.quantity());
        } catch (IllegalStateException exception) {
            throw ApiException.conflict("INSUFFICIENT_STOCK", product.getName() + " 库存不足");
        }

        Set<Long> selectedIds = request.optionIds() == null
                ? Set.of()
                : new HashSet<>(request.optionIds());
        Set<Long> validIds = product.getOptionGroups().stream()
                .flatMap(group -> group.getOptions().stream())
                .map(ProductOption::getId)
                .collect(Collectors.toSet());
        if (!validIds.containsAll(selectedIds)) {
            throw ApiException.badRequest("OPTION_INVALID", product.getName() + " 包含无效规格");
        }

        List<String> summaries = new ArrayList<>();
        BigDecimal unitPrice = product.getVirtualPrice();
        for (ProductOptionGroup group : product.getOptionGroups()) {
            List<ProductOption> selected = group.getOptions().stream()
                    .filter(option -> selectedIds.contains(option.getId()))
                    .toList();
            if (selected.size() < group.getMinSelect() || selected.size() > group.getMaxSelect()) {
                throw ApiException.badRequest("OPTION_SELECTION_INVALID",
                        product.getName() + " 的“" + group.getName() + "”选择数量不正确");
            }
            if (group.isRequired() && selected.isEmpty()) {
                throw ApiException.badRequest("OPTION_REQUIRED",
                        product.getName() + " 必须选择“" + group.getName() + "”");
            }
            if (selected.stream().anyMatch(option -> !option.isEnabled())) {
                throw ApiException.conflict("OPTION_UNAVAILABLE", product.getName() + " 的部分规格已不可用");
            }
            if (!selected.isEmpty()) {
                summaries.add(group.getName() + "：" + selected.stream()
                        .map(ProductOption::getName)
                        .collect(Collectors.joining("、")));
                unitPrice = unitPrice.add(selected.stream()
                        .map(ProductOption::getExtraPrice)
                        .reduce(BigDecimal.ZERO, BigDecimal::add));
            }
        }
        return new PreparedLine(
                product,
                request.quantity(),
                unitPrice,
                String.join("；", summaries)
        );
    }

    private String generateOrderNo() {
        return "LOVE" + ORDER_DATE.format(java.time.Instant.now())
                + String.format("%04d", RANDOM.nextInt(10_000));
    }

    private String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private record PreparedLine(
            Product product,
            int quantity,
            BigDecimal unitPrice,
            String optionSummary
    ) {
    }
}

