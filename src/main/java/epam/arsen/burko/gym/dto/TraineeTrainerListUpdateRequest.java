package epam.arsen.burko.gym.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record TraineeTrainerListUpdateRequest(
        @NotNull(message = "Trainers list is required")
        List<@Valid TrainerUsernameRequest> trainers
) {
}


