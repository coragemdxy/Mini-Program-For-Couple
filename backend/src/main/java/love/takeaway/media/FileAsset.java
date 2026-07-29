package love.takeaway.media;

import jakarta.persistence.*;
import love.takeaway.user.UserAccount;

import java.time.Instant;

@Entity
@Table(name = "file_asset")
public class FileAsset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String storageKey;

    @Column(nullable = false, length = 500)
    private String publicUrl;

    @Column(nullable = false, length = 100)
    private String contentType;

    @Column(nullable = false)
    private long sizeBytes;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "uploader_id", nullable = false)
    private UserAccount uploader;

    @Column(nullable = false)
    private Instant createdAt;

    protected FileAsset() {
    }

    public FileAsset(
            String storageKey,
            String publicUrl,
            String contentType,
            long sizeBytes,
            UserAccount uploader
    ) {
        this.storageKey = storageKey;
        this.publicUrl = publicUrl;
        this.contentType = contentType;
        this.sizeBytes = sizeBytes;
        this.uploader = uploader;
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getStorageKey() {
        return storageKey;
    }

    public String getPublicUrl() {
        return publicUrl;
    }

    public String getContentType() {
        return contentType;
    }

    public long getSizeBytes() {
        return sizeBytes;
    }
}

