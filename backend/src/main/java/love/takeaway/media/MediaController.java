package love.takeaway.media;

import love.takeaway.common.ApiException;
import love.takeaway.security.AppPrincipal;
import love.takeaway.security.AuthenticatedUser;
import love.takeaway.user.UserAccount;
import love.takeaway.user.UserAccountRepository;
import love.takeaway.user.UserRole;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api")
public class MediaController {

    private final LocalFileStorageService storage;
    private final FileAssetRepository assets;
    private final AuthenticatedUser authenticatedUser;
    private final UserAccountRepository users;

    public MediaController(
            LocalFileStorageService storage,
            FileAssetRepository assets,
            AuthenticatedUser authenticatedUser,
            UserAccountRepository users
    ) {
        this.storage = storage;
        this.assets = assets;
        this.authenticatedUser = authenticatedUser;
        this.users = users;
    }

    @PostMapping(path = "/merchant/media", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public MediaResponse upload(@RequestPart("file") MultipartFile file) {
        AppPrincipal principal = authenticatedUser.requireRole(UserRole.MERCHANT);
        UserAccount uploader = users.findById(principal.userId())
                .orElseThrow(() -> ApiException.notFound("USER_NOT_FOUND", "用户不存在"));
        LocalFileStorageService.StoredFile stored = storage.store(file);
        String publicUrl = "/api/media/" + stored.key();
        FileAsset asset = assets.save(new FileAsset(
                stored.key(),
                publicUrl,
                stored.contentType(),
                stored.size(),
                uploader
        ));
        return new MediaResponse(asset.getId(), publicUrl, stored.contentType(), stored.size());
    }

    @GetMapping("/media/{key}")
    public ResponseEntity<Resource> download(@PathVariable String key) {
        FileAsset asset = assets.findByStorageKey(key)
                .orElseThrow(() -> ApiException.notFound("FILE_NOT_FOUND", "图片不存在"));
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(asset.getContentType()))
                .cacheControl(CacheControl.maxAge(30, TimeUnit.DAYS).cachePublic())
                .body(storage.load(key));
    }

    public record MediaResponse(Long id, String url, String contentType, long size) {
    }
}

