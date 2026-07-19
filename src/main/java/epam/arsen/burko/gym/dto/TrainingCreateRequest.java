package epam.arsen.burko.gym.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record TrainingCreateRequest(
        @NotBlank(message = "Trainee username is required")
        String traineeUsername,

        @NotBlank(message = "Trainer username is required")
        String trainerUsername,

        @NotBlank(message = "Training name is required")
        String trainingName,

        @NotNull(message = "Training date is required")
        LocalDate trainingDate,

        @NotNull(message = "Training duration is required")
        @Min(value = 1, message = "Training duration must be greater than 0")
        Integer trainingDuration
) {
}


