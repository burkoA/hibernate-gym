package epam.arsen.burko.gym.repository;

import epam.arsen.burko.gym.entity.Training;

import java.time.LocalDate;
import java.util.List;

public interface TrainingRepositoryCustom {
    List<Training> findTraineeTrainingsByCriteria(
            String username,
            LocalDate fromDate,
            LocalDate toDate,
            String trainerName,
            String trainingType
    );

    List<Training> findTrainerTrainingsByCriteria(
            String username,
            LocalDate fromDate,
            LocalDate toDate,
            String traineeName
    );
}

