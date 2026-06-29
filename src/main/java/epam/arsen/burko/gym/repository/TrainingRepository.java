package epam.arsen.burko.gym.repository;

import epam.arsen.burko.gym.entity.Training;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface TrainingRepository extends JpaRepository<Training, Long> {
    @Query("SELECT t FROM Training t WHERE t.trainee.username = :username " +
            "AND (:fromDate IS NULL OR t.trainingDate >= :fromDate) " +
            "AND (:toDate IS NULL OR t.trainingDate <= :toDate) " +
            "AND (:trainerName IS NULL OR t.trainer.firstName LIKE %:trainerName% OR t.trainer.lastName LIKE %:trainerName%) " +
            "AND (:trainingType IS NULL OR t.trainingType.trainingTypeName = :trainingType)")
    List<Training> findTraineeTrainingsByCriteria(
            @Param("username") String username,
            @Param("fromDate") Date fromDate,
            @Param("toDate") Date toDate,
            @Param("trainerName") String trainerName,
            @Param("trainingType") String trainingType
    );

    @Query("SELECT t FROM Training t WHERE t.trainer.username = :username " +
            "AND (:fromDate IS NULL OR t.trainingDate >= :fromDate) " +
            "AND (:toDate IS NULL OR t.trainingDate <= :toDate) " +
            "AND (:traineeName IS NULL OR t.trainee.firstName LIKE %:traineeName% OR t.trainee.lastName LIKE %:traineeName%)")
    List<Training> findTrainerTrainingsByCriteria(
            @Param("username") String username,
            @Param("fromDate") Date fromDate,
            @Param("toDate") Date toDate,
            @Param("traineeName") String traineeName
    );
}
