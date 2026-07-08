package epam.arsen.burko.gym.service;

import epam.arsen.burko.gym.dto.TrainingDto;
import epam.arsen.burko.gym.dto.TrainingCreateRequest;
import epam.arsen.burko.gym.dto.TraineeTrainingResponse;
import epam.arsen.burko.gym.dto.TrainerTrainingResponse;
import epam.arsen.burko.gym.dto.TrainingTypeResponse;
import epam.arsen.burko.gym.entity.Trainer;
import epam.arsen.burko.gym.entity.Training;
import epam.arsen.burko.gym.exception.SpecializationNotFoundException;
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

    @Transactional
    public void addTraining(TrainingCreateRequest request) {
        log.info("Attempting to add new training '{}' for trainee: {} with trainer: {}",
                request.trainingName(), request.traineeUsername(), request.trainerUsername());

        Trainer trainer = trainerRepository.findByUsername(request.trainerUsername())
                .orElseThrow(() -> new TrainerNotFoundException("Trainer not found"));

        if (trainer.getSpecialization() == null) {
            throw new SpecializationNotFoundException("Trainer specialization not found");
        }

        Training training = new Training();
        training.setTrainee(traineeRepository.findByUsername(request.traineeUsername())
                .orElseThrow(() -> new TraineeNotFoundException("Trainee not found")));
        training.setTrainer(trainer);
        training.setTrainingName(request.trainingName());
        training.setTrainingType(trainer.getSpecialization());
        training.setTrainingDate(request.trainingDate());
        training.setTrainingDuration(request.trainingDuration());

        trainingRepository.save(training);
        log.info("Successfully added training for trainee: {} with trainer: {}",
                request.traineeUsername(), request.trainerUsername());
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

    @Transactional(readOnly = true)
    public List<TraineeTrainingResponse> getTraineeTrainings(String username, LocalDate fromDate,
                                                             LocalDate toDate, String trainerName, String trainingType) {
        log.info("Fetching trainings for trainee: {} with applied filters", username);

        traineeRepository.findByUsername(username)
                .orElseThrow(() -> new TraineeNotFoundException("Trainee not found"));

        return trainingRepository.findTraineeTrainingsByCriteria(username, fromDate, toDate, trainerName, trainingType)
                .stream()
                .map(training -> new TraineeTrainingResponse(
                        training.getTrainingName(),
                        training.getTrainingDate(),
                        training.getTrainingType().getId(),
                        training.getTrainingType().getTrainingTypeName(),
                        training.getTrainingDuration(),
                        training.getTrainer().getFirstName() + " " + training.getTrainer().getLastName()
                ))
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

    @Transactional(readOnly = true)
    public List<TrainerTrainingResponse> getTrainerTrainings(String username, LocalDate fromDate,
                                                             LocalDate toDate, String traineeName) {
        log.info("Fetching trainings for trainer: {} with applied filters", username);

        trainerRepository.findByUsername(username)
                .orElseThrow(() -> new TrainerNotFoundException("Trainer not found"));

        return trainingRepository.findTrainerTrainingsByCriteria(username, fromDate, toDate, traineeName)
                .stream()
                .map(training -> new TrainerTrainingResponse(
                        training.getTrainingName(),
                        training.getTrainingDate(),
                        training.getTrainingType().getId(),
                        training.getTrainingType().getTrainingTypeName(),
                        training.getTrainingDuration(),
                        training.getTrainee().getFirstName() + " " + training.getTrainee().getLastName()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TrainingTypeResponse> getTrainingTypes() {
        log.info("Fetching all training types");
        return trainingTypeRepository.findAll().stream()
                .map(trainingType -> new TrainingTypeResponse(
                        trainingType.getId(),
                        trainingType.getTrainingTypeName()
                ))
                .toList();
    }
}
