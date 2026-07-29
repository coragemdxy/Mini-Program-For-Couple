package love.takeaway.catalog;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "category")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String name;

    @Column(length = 500)
    private String iconUrl;

    @Column(nullable = false)
    private int sortOrder;

    @Column(nullable = false)
    private boolean enabled;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @Version
    private long version;

    protected Category() {
    }

    public Category(String name, String iconUrl, int sortOrder, boolean enabled) {
        this.name = name;
        this.iconUrl = iconUrl;
        this.sortOrder = sortOrder;
        this.enabled = enabled;
        this.createdAt = Instant.now();
        this.updatedAt = createdAt;
    }

    public void update(String name, String iconUrl, int sortOrder, boolean enabled) {
        this.name = name;
        this.iconUrl = iconUrl;
        this.sortOrder = sortOrder;
        this.enabled = enabled;
        this.updatedAt = Instant.now();
    }

    public void disable() {
        this.enabled = false;
        this.updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public long getVersion() {
        return version;
    }
}

