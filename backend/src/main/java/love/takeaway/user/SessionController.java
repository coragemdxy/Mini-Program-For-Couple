package love.takeaway.user;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import static love.takeaway.user.SessionDtos.*;

@RestController
@RequestMapping("/api")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @GetMapping("/session")
    public SessionResponse session() {
        return sessionService.getSession();
    }

    @PostMapping("/invitations/redeem")
    public SessionResponse redeem(@Valid @RequestBody RedeemInvitationRequest request) {
        return sessionService.redeem(request);
    }
}

