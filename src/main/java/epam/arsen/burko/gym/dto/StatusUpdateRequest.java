package epam.arsen.burko.gym.dto;

import jakarta.validation.constraints.NotNull;

public record StatusUpdateRequest(
        @NotNull(message = "isActive is required")
        Boolean isActive
) {
}

