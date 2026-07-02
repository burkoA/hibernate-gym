package epam.arsen.burko.gym.dto;

import java.time.LocalDate;

public record TrainingDto(
        Long id,
        String traineeUsername,
        String trainerUsername,
        String trainingName,
        Long trainingTypeId,
        String trainingTypeName,
        LocalDate trainingDate,
        int trainingDuration
) {
}

