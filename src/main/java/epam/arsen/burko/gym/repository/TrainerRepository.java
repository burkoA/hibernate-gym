package epam.arsen.burko.gym.repository;

import epam.arsen.burko.gym.entity.Trainer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrainerRepository extends JpaRepository<Trainer, Long> {
    Optional<Trainer> findByUsername(String username);

    @Query("SELECT t FROM Trainer t WHERE t NOT IN " +
            "(SELECT tr FROM Trainee tn JOIN tn.trainers tr WHERE tn.username = :username)")
    List<Trainer> findTrainersNotAssignedToTrainee(@Param("username") String username);
}
