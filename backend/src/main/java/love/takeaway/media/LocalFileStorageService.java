package love.takeaway.media;

import love.takeaway.common.ApiException;
import love.takeaway.config.AppProperties;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class LocalFileStorageService {

    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/webp");
    private static final Map<String, String> EXTENSIONS = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/webp", ".webp"
    );

    private final Path root;

    public LocalFileStorageService(AppProperties properties) {
        this.root = Path.of(properties.getStorage().getDirectory()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(root);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to create upload directory " + root, exception);
        }
    }

    public StoredFile store(MultipartFile file) {
        if (file.isEmpty()) {
            throw ApiException.badRequest("FILE_EMPTY", "请选择需要上传的图片");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw ApiException.badRequest("FILE_TYPE_UNSUPPORTED", "只支持 JPG、PNG 和 WebP 图片");
        }
        String key = UUID.randomUUID().toString().replace("-", "") + EXTENSIONS.get(contentType);
        Path destination = root.resolve(key).normalize();
        if (!destination.startsWith(root)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "FILE_PATH_INVALID", "文件路径不合法");
        }
        try {
            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
            return new StoredFile(key, contentType, file.getSize());
        } catch (IOException exception) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "FILE_STORE_FAILED", "图片保存失败");
        }
    }

    public Resource load(String key) {
        if (key == null || !key.matches("[a-f0-9]{32}\\.(jpg|png|webp)")) {
            throw ApiException.notFound("FILE_NOT_FOUND", "图片不存在");
        }
        try {
            Path path = root.resolve(key).normalize();
            if (!path.startsWith(root) || !Files.isRegularFile(path)) {
                throw ApiException.notFound("FILE_NOT_FOUND", "图片不存在");
            }
            return new UrlResource(path.toUri());
        } catch (IOException exception) {
            throw ApiException.notFound("FILE_NOT_FOUND", "图片不存在");
        }
    }

    public record StoredFile(String key, String contentType, long size) {
    }
}

