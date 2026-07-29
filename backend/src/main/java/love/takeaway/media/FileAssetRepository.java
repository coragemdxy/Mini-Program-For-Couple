package love.takeaway.media;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FileAssetRepository extends JpaRepository<FileAsset, Long> {
    Optional<FileAsset> findByStorageKey(String storageKey);
}

