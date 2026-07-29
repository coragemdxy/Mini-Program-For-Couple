package love.takeaway.catalog;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "product_option")
public class ProductOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "option_group_id", nullable = false)
    private ProductOptionGroup group;

    @Column(nullable = false, length = 80)
    private String name;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal extraPrice;

    @Column(nullable = false)
    private boolean enabled;

    @Column(nullable = false)
    private int sortOrder;

    protected ProductOption() {
    }

    ProductOption(
            ProductOptionGroup group,
            String name,
            BigDecimal extraPrice,
            boolean enabled,
            int sortOrder
    ) {
        this.group = group;
        this.name = name;
        this.extraPrice = extraPrice;
        this.enabled = enabled;
        this.sortOrder = sortOrder;
    }

    public Long getId() {
        return id;
    }

    public ProductOptionGroup getGroup() {
        return group;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getExtraPrice() {
        return extraPrice;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getSortOrder() {
        return sortOrder;
    }
}

