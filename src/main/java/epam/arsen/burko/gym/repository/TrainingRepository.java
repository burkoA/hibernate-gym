package epam.arsen.burko.gym.repository;

import epam.arsen.burko.gym.entity.Training;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TrainingRepository extends JpaRepository<Training, Long>, TrainingRepositoryCustom {
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
