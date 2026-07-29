package love.takeaway.notification;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notifications;

    public NotificationController(NotificationService notifications) {
        this.notifications = notifications;
    }

    @PostMapping("/consents")
    public NotificationService.ConsentResponse consent(@Valid @RequestBody ConsentRequest request) {
        return notifications.recordConsent(new NotificationService.ConsentRequest(request.type(), request.result()));
    }

    @GetMapping("/settings")
    public List<NotificationService.ConsentResponse> settings() {
        return notifications.settings();
    }

    public record ConsentRequest(
            @NotNull NotificationType type,
            @NotBlank String result
    ) {
    }
}

