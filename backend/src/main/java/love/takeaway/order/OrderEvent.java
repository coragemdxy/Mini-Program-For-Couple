package love.takeaway.order;

import jakarta.persistence.*;
import love.takeaway.user.UserAccount;

import java.time.Instant;

@Entity
@Table(name = "order_event")
public class OrderEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private CustomerOrder order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "operator_id", nullable = false)
    private UserAccount operator;

    @Enumerated(EnumType.STRING)
    @Column(length = 32)
    private OrderStatus oldStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private OrderStatus newStatus;

    @Column(length = 500)
    private String message;

    @Column(nullable = false)
    private Instant createdAt;

    protected OrderEvent() {
    }

    OrderEvent(
            CustomerOrder order,
            UserAccount operator,
            OrderStatus oldStatus,
            OrderStatus newStatus,
            String message
    ) {
        this.order = order;
        this.operator = operator;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.message = message;
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public UserAccount getOperator() {
        return operator;
    }

    public OrderStatus getOldStatus() {
        return oldStatus;
    }

    public OrderStatus getNewStatus() {
        return newStatus;
    }

    public String getMessage() {
        return message;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}

