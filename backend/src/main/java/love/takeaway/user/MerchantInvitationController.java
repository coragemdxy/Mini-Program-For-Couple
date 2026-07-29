package love.takeaway.user;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/merchant/invitations")
public class MerchantInvitationController {

    private final InvitationManagementService invitations;

    public MerchantInvitationController(InvitationManagementService invitations) {
        this.invitations = invitations;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InvitationManagementService.InvitationResponse create(
            @Valid @RequestBody InvitationManagementService.CreateInvitationRequest request
    ) {
        return invitations.create(request);
    }
}
