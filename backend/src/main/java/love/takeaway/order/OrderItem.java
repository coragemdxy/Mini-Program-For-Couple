package love.takeaway.order;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "order_item")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private CustomerOrder order;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false, length = 120)
    private String productNameSnapshot;

    @Column(length = 500)
    private String productImageSnapshot;

    @Column(length = 1000)
    private String optionSnapshot;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPriceSnapshot;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal lineTotal;

    protected OrderItem() {
    }

    OrderItem(
            CustomerOrder order,
            Long productId,
            String productName,
            String productImage,
            String optionSnapshot,
            BigDecimal unitPrice,
            int quantity
    ) {
        this.order = order;
        this.productId = productId;
        this.productNameSnapshot = productName;
        this.productImageSnapshot = productImage;
        this.optionSnapshot = optionSnapshot;
        this.unitPriceSnapshot = unitPrice;
        this.quantity = quantity;
        this.lineTotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public Long getId() {
        return id;
    }

    public Long getProductId() {
        return productId;
    }

    public String getProductNameSnapshot() {
        return productNameSnapshot;
    }

    public String getProductImageSnapshot() {
        return productImageSnapshot;
    }

    public String getOptionSnapshot() {
        return optionSnapshot;
    }

    public BigDecimal getUnitPriceSnapshot() {
        return unitPriceSnapshot;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getLineTotal() {
        return lineTotal;
    }
}

