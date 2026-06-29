package epam.arsen.burko.gym.service;

import epam.arsen.burko.gym.entity.Training;
import epam.arsen.burko.gym.repository.TraineeRepository;
import epam.arsen.burko.gym.repository.TrainerRepository;
import epam.arsen.burko.gym.repository.TrainingRepository;
import epam.arsen.burko.gym.repository.TrainingTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TrainingService {
    private final TrainingRepository trainingRepository;
    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final TrainingTypeRepository trainingTypeRepository;
    private final AuthService auth;

    @Transactional
    public Training add(String authUsername, String authPassword, String traineeUsername,
                        String trainerUsername, String name, Long typeId, Date date, Integer duration) {
        auth.validate(authUsername, authPassword);

        Training training = new Training();
        training.setTrainee(traineeRepository.findByUsername(traineeUsername).orElseThrow());
        training.setTrainer(trainerRepository.findByUsername(trainerUsername).orElseThrow());
        training.setTrainingName(name);
        training.setTrainingType(trainingTypeRepository.findById(typeId).orElseThrow());
        training.setTrainingDate(date);
        training.setTrainingDuration(duration);

        return trainingRepository.save(training);
    }

    public List<Training> getTraineeTrainings(String username, String password, Date fromDate,
                                              Date toDate, String trainerName, String trainingType) {
        auth.validate(username, password);
        return trainingRepository.findTraineeTrainingsByCriteria(username, fromDate, toDate, trainerName, trainingType);
    }

    public List<Training> getTrainerTrainings(String username, String password, Date fromDate,
                                              Date toDate, String traineeName) {
        auth.validate(username, password);
        return trainingRepository.findTrainerTrainingsByCriteria(username, fromDate, toDate, traineeName);
    }
}
