package love.takeaway.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class SessionDtos {

    private SessionDtos() {
    }

    public record SessionResponse(
            String state,
            Long userId,
            UserRole role,
            String nickname,
            String openidHint
    ) {
    }

    public record RedeemInvitationRequest(
            @NotBlank @Size(min = 4, max = 80) String code,
            @NotBlank @Size(min = 1, max = 40) String nickname
    ) {
    }
}

