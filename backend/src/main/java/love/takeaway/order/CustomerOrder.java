package love.takeaway.order;

import jakarta.persistence.*;
import love.takeaway.user.UserAccount;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "customer_order")
public class CustomerOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String orderNo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private UserAccount customer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private OrderStatus status;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalVirtualPrice;

    @Column(length = 500)
    private String remark;

    @Column(nullable = false, length = 64)
    private String idempotencyKey;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @Version
    private long version;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<OrderItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC, id ASC")
    private List<OrderEvent> events = new ArrayList<>();

    protected CustomerOrder() {
    }

    public CustomerOrder(
            String orderNo,
            UserAccount customer,
            BigDecimal totalVirtualPrice,
            String remark,
            String idempotencyKey
    ) {
        this.orderNo = orderNo;
        this.customer = customer;
        this.status = OrderStatus.SUBMITTED;
        this.totalVirtualPrice = totalVirtualPrice;
        this.remark = remark;
        this.idempotencyKey = idempotencyKey;
        this.createdAt = Instant.now();
        this.updatedAt = createdAt;
    }

    public void addItem(
            Long productId,
            String productName,
            String productImage,
            String optionSnapshot,
            BigDecimal unitPrice,
            int quantity
    ) {
        items.add(new OrderItem(
                this,
                productId,
                productName,
                productImage,
                optionSnapshot,
                unitPrice,
                quantity
        ));
    }

    public void addEvent(
            UserAccount operator,
            OrderStatus oldStatus,
            OrderStatus newStatus,
            String message
    ) {
        events.add(new OrderEvent(this, operator, oldStatus, newStatus, message));
    }

    public void transition(UserAccount operator, OrderStatus target, String message) {
        status.requireTransitionTo(target);
        OrderStatus previous = status;
        status = target;
        updatedAt = Instant.now();
        addEvent(operator, previous, target, message);
    }

    public Long getId() {
        return id;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public UserAccount getCustomer() {
        return customer;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public BigDecimal getTotalVirtualPrice() {
        return totalVirtualPrice;
    }

    public String getRemark() {
        return remark;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public List<OrderItem> getItems() {
        return List.copyOf(items);
    }

    public List<OrderEvent> getEvents() {
        return List.copyOf(events);
    }
}

