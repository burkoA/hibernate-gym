package epam.arsen.burko.gym.repository;

import epam.arsen.burko.gym.entity.Training;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Repository
public class TrainingRepositoryImpl implements TrainingRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Training> findTraineeTrainingsByCriteria(String username, LocalDate fromDate, LocalDate toDate, String trainerName,
                                                         String trainingType) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Training> query = cb.createQuery(Training.class);
        Root<Training> training = query.from(Training.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(training.get("trainee").get("username"), username));

        if (fromDate != null) {
            predicates.add(cb.greaterThanOrEqualTo(training.get("trainingDate"), fromDate));
        }

        if (toDate != null) {
            predicates.add(cb.lessThanOrEqualTo(training.get("trainingDate"), toDate));
        }

        if (trainerName != null) {
            Join<Object, Object> trainer = training.join("trainer");
            String trainerPattern = "%" + trainerName.toLowerCase(Locale.ROOT) + "%";
            predicates.add(cb.or(
                    cb.like(cb.lower(trainer.get("firstName")), trainerPattern),
                    cb.like(cb.lower(trainer.get("lastName")), trainerPattern)
            ));
        }

        if (trainingType != null) {
            predicates.add(cb.equal(training.get("trainingType").get("trainingTypeName"), trainingType));
        }

        query.select(training).where(predicates.toArray(new Predicate[0]));
        TypedQuery<Training> typedQuery = entityManager.createQuery(query);
        return typedQuery.getResultList();
    }

    @Override
    public List<Training> findTrainerTrainingsByCriteria(String username, LocalDate fromDate, LocalDate toDate,
                                                         String traineeName) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Training> query = cb.createQuery(Training.class);
        Root<Training> training = query.from(Training.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(training.get("trainer").get("username"), username));

        if (fromDate != null) {
            predicates.add(cb.greaterThanOrEqualTo(training.get("trainingDate"), fromDate));
        }

        if (toDate != null) {
            predicates.add(cb.lessThanOrEqualTo(training.get("trainingDate"), toDate));
        }

        if (traineeName != null) {
            Join<Object, Object> trainee = training.join("trainee");
            String traineePattern = "%" + traineeName.toLowerCase(Locale.ROOT) + "%";
            predicates.add(cb.or(
                    cb.like(cb.lower(trainee.get("firstName")), traineePattern),
                    cb.like(cb.lower(trainee.get("lastName")), traineePattern)
            ));
        }

        query.select(training).where(predicates.toArray(new Predicate[0]));
        TypedQuery<Training> typedQuery = entityManager.createQuery(query);
        return typedQuery.getResultList();
    }
}


