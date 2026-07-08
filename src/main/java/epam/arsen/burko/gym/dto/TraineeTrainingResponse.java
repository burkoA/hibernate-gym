package epam.arsen.burko.gym.dto;

import java.time.LocalDate;

public record TraineeTrainingResponse(
        String trainingName,
        LocalDate trainingDate,
        Long trainingTypeId,
        String trainingTypeName,
        int trainingDuration,
        String trainerName
) {
}

