package epam.arsen.burko.gym.dto;

import jakarta.validation.constraints.NotBlank;

public record TrainerUsernameRequest(
        @NotBlank(message = "Trainer username is required")
        String username
) {
}

