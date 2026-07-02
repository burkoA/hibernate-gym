package epam.arsen.burko.gym.service;

import epam.arsen.burko.gym.dto.TrainingDto;
import epam.arsen.burko.gym.entity.Training;
import epam.arsen.burko.gym.exception.TraineeNotFoundException;
import epam.arsen.burko.gym.exception.TrainerNotFoundException;
import epam.arsen.burko.gym.exception.TrainingTypeNotFoundException;
import epam.arsen.burko.gym.repository.TraineeRepository;
import epam.arsen.burko.gym.repository.TrainerRepository;
import epam.arsen.burko.gym.repository.TrainingRepository;
import epam.arsen.burko.gym.repository.TrainingTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static epam.arsen.burko.gym.dto.GymDtoMapper.toDto;

@Service
@Slf4j
@RequiredArgsConstructor
public class TrainingService {
    private final TrainingRepository trainingRepository;
    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final TrainingTypeRepository trainingTypeRepository;
    private final AuthService auth;

    @Transactional
    public TrainingDto add(String authUsername, String authPassword, String traineeUsername,
                           String trainerUsername, String name, Long typeId, LocalDate date, int duration) {
        log.info("Attempting to add new training '{}' for trainee: {} with trainer: {}", name, traineeUsername, trainerUsername);

        auth.validate(authUsername, authPassword);

        Training training = new Training();
        training.setTrainee(traineeRepository.findByUsername(traineeUsername)
                .orElseThrow(() -> new TraineeNotFoundException("Trainee not found")));
        training.setTrainer(trainerRepository.findByUsername(trainerUsername)
                .orElseThrow(() -> new TrainerNotFoundException("Trainer not found")));
        training.setTrainingName(name);
        training.setTrainingType(trainingTypeRepository.findById(typeId)
                .orElseThrow(() -> new TrainingTypeNotFoundException("Training type not found")));
        training.setTrainingDate(date);
        training.setTrainingDuration(duration);

        log.info("Successfully added training with ID: {}", training.getId());

        return toDto(trainingRepository.save(training));
    }

    public List<TrainingDto> getTraineeTrainings(String username, String password, LocalDate fromDate,
                                                 LocalDate toDate, String trainerName, String trainingType) {
        log.info("Fetching trainings for trainee: {} with applied filters", username);
        auth.validate(username, password);
        return trainingRepository.findTraineeTrainingsByCriteria(username, fromDate, toDate, trainerName, trainingType)
                .stream()
                .map(training -> toDto(training))
                .toList();
    }

    public List<TrainingDto> getTrainerTrainings(String username, String password, LocalDate fromDate,
                                                 LocalDate toDate, String traineeName) {
        log.info("Fetching trainings for trainer: {} with applied filters", username);
        auth.validate(username, password);
        return trainingRepository.findTrainerTrainingsByCriteria(username, fromDate, toDate, traineeName)
                .stream()
                .map(training -> toDto(training))
                .toList();
    }
}
