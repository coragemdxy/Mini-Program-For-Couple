package love.takeaway.catalog;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Entity
@Table(name = "product_option_group")
public class ProductOptionGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, length = 80)
    private String name;

    @Column(name = "required_selection", nullable = false)
    private boolean required;

    @Column(nullable = false)
    private int minSelect;

    @Column(nullable = false)
    private int maxSelect;

    @Column(nullable = false)
    private int sortOrder;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC, id ASC")
    private List<ProductOption> options = new ArrayList<>();

    protected ProductOptionGroup() {
    }

    public ProductOptionGroup(
            String name,
            boolean required,
            int minSelect,
            int maxSelect,
            int sortOrder
    ) {
        this.name = name;
        this.required = required;
        this.minSelect = minSelect;
        this.maxSelect = maxSelect;
        this.sortOrder = sortOrder;
    }

    public void addOption(String name, BigDecimal extraPrice, boolean enabled, int sortOrder) {
        options.add(new ProductOption(this, name, extraPrice, enabled, sortOrder));
        options.sort(Comparator.comparingInt(ProductOption::getSortOrder));
    }

    void attachTo(Product product) {
        this.product = product;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isRequired() {
        return required;
    }

    public int getMinSelect() {
        return minSelect;
    }

    public int getMaxSelect() {
        return maxSelect;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public List<ProductOption> getOptions() {
        return List.copyOf(options);
    }
}

