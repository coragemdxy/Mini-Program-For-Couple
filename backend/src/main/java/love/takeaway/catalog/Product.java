package love.takeaway.catalog;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Entity
@Table(name = "product")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(length = 500)
    private String coverUrl;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal virtualPrice;

    private Integer stock;

    @Column(nullable = false)
    private boolean enabled;

    @Column(nullable = false)
    private boolean recommended;

    @Column(nullable = false)
    private int sortOrder;

    @Column(length = 500)
    private String searchKeywords;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @Version
    private long version;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC, id ASC")
    private List<ProductOptionGroup> optionGroups = new ArrayList<>();

    protected Product() {
    }

    public Product(
            Category category,
            String name,
            String description,
            String coverUrl,
            BigDecimal virtualPrice,
            Integer stock,
            boolean enabled,
            boolean recommended,
            int sortOrder,
            String searchKeywords
    ) {
        this.category = category;
        this.name = name;
        this.description = description;
        this.coverUrl = coverUrl;
        this.virtualPrice = virtualPrice;
        this.stock = stock;
        this.enabled = enabled;
        this.recommended = recommended;
        this.sortOrder = sortOrder;
        this.searchKeywords = searchKeywords;
        this.createdAt = Instant.now();
        this.updatedAt = createdAt;
    }

    public void update(
            Category category,
            String name,
            String description,
            String coverUrl,
            BigDecimal virtualPrice,
            Integer stock,
            boolean enabled,
            boolean recommended,
            int sortOrder,
            String searchKeywords
    ) {
        this.category = category;
        this.name = name;
        this.description = description;
        this.coverUrl = coverUrl;
        this.virtualPrice = virtualPrice;
        this.stock = stock;
        this.enabled = enabled;
        this.recommended = recommended;
        this.sortOrder = sortOrder;
        this.searchKeywords = searchKeywords;
        this.updatedAt = Instant.now();
    }

    public void replaceOptionGroups(List<ProductOptionGroup> groups) {
        optionGroups.clear();
        groups.stream()
                .sorted(Comparator.comparingInt(ProductOptionGroup::getSortOrder))
                .forEach(group -> {
                    group.attachTo(this);
                    optionGroups.add(group);
                });
        this.updatedAt = Instant.now();
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        this.updatedAt = Instant.now();
    }

    public void reserveStock(int quantity) {
        if (stock == null) {
            return;
        }
        if (stock < quantity) {
            throw new IllegalStateException("INSUFFICIENT_STOCK");
        }
        stock -= quantity;
        updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Category getCategory() {
        return category;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public BigDecimal getVirtualPrice() {
        return virtualPrice;
    }

    public Integer getStock() {
        return stock;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isRecommended() {
        return recommended;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public String getSearchKeywords() {
        return searchKeywords;
    }

    public long getVersion() {
        return version;
    }

    public List<ProductOptionGroup> getOptionGroups() {
        return List.copyOf(optionGroups);
    }
}

