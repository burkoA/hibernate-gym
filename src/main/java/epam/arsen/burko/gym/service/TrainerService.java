package epam.arsen.burko.gym.service;

import epam.arsen.burko.gym.dto.TrainerDto;
import epam.arsen.burko.gym.dto.TrainerProfileResponse;
import epam.arsen.burko.gym.dto.TrainerUpdateRequest;
import epam.arsen.burko.gym.entity.Trainer;
import epam.arsen.burko.gym.entity.Trainee;
import epam.arsen.burko.gym.entity.Training;
import epam.arsen.burko.gym.entity.TrainingType;
import epam.arsen.burko.gym.entity.User;
import epam.arsen.burko.gym.exception.SpecializationNotFoundException;
import epam.arsen.burko.gym.exception.RoleConflictException;
import epam.arsen.burko.gym.exception.TrainerNotFoundException;
import epam.arsen.burko.gym.repository.TrainerRepository;
import epam.arsen.burko.gym.repository.TrainingRepository;
import epam.arsen.burko.gym.repository.TrainingTypeRepository;
import epam.arsen.burko.gym.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

import static epam.arsen.burko.gym.dto.GymDtoMapper.toProfileResponse;

@Service
@Slf4j
@RequiredArgsConstructor
public class TrainerService {
    private static final String TRAINER_NOT_FOUND_MESSAGE = "Trainer not found";

    private final TrainerRepository trainerRepository;
    private final TrainingRepository trainingRepository;
    private final IdentityGenerationService identityService;
    private final TrainingTypeRepository trainingTypeRepository;
    private final UserRepository userRepository;


    @Transactional
    public TrainerDto createTrainer(String firstName, String lastName, Long specializationId) {
        log.info("Creating new trainer profile for: {} {}", firstName, lastName);

        String baseUsername = firstName + "." + lastName;
        validateNoTraineeExists(baseUsername);

        TrainingType specialization = trainingTypeRepository.findById(specializationId)
                .orElseThrow(() -> new SpecializationNotFoundException("Specialization not found"));

        Trainer trainer = new Trainer();
        trainer.setFirstName(firstName);
        trainer.setLastName(lastName);
        trainer.setUsername(identityService.generateUsername(firstName, lastName));
        String plainPassword = identityService.generatePassword();
        trainer.setPassword(identityService.encodePassword(plainPassword));
        trainer.setIsActive(true);
        trainer.setSpecialization(specialization);

        Trainer savedTrainer = trainerRepository.save(trainer);
        log.info("Successfully created trainer with username: {}", trainer.getUsername());

        return new TrainerDto(
                savedTrainer.getId(),
                savedTrainer.getFirstName(),
                savedTrainer.getLastName(),
                savedTrainer.getUsername(),
                plainPassword,
                savedTrainer.getIsActive(),
                specialization.getId(),
                specialization.getTrainingTypeName()
        );
    }

    private void validateNoTraineeExists(String baseUsername) {
        List<User> existingUsers = userRepository.findByUsernameStartingWith(baseUsername);
        boolean traineeExists = existingUsers.stream().anyMatch(epam.arsen.burko.gym.entity.Trainee.class::isInstance);
        if (traineeExists) {
            throw new RoleConflictException("Trainee profile already exists for this user");
        }
    }

    @Transactional(readOnly = true)
    public TrainerProfileResponse getProfile(String username) {
        log.info("Fetching public profile for trainer: {}", username);
        return toProfileResponse(trainerRepository.findByUsername(username)
                .orElseThrow(() -> new TrainerNotFoundException(TRAINER_NOT_FOUND_MESSAGE)));
    }

    @Transactional
    public void toggleStatus(String username, boolean isActive) {
        log.info("Toggling active status for trainer: {} to {}", username, isActive);

        Trainer trainer = trainerRepository.findByUsername(username)
                .orElseThrow(() -> new TrainerNotFoundException(TRAINER_NOT_FOUND_MESSAGE));
        trainer.setIsActive(isActive);

        trainerRepository.save(trainer);
        log.info("Successfully updated status for trainer: {}", username);
    }

    @Transactional
    public TrainerProfileResponse updateProfile(String username, TrainerUpdateRequest request) {
        log.info("Updating profile for trainer: {}", username);

        Trainer trainer = trainerRepository.findByUsername(username)
                .orElseThrow(() -> new TrainerNotFoundException(TRAINER_NOT_FOUND_MESSAGE));

        trainer.setFirstName(request.firstName());
        trainer.setLastName(request.lastName());
        trainer.setIsActive(request.isActive());

        log.info("Successfully updated profile for trainer: {}", username);
        return toProfileResponse(trainerRepository.save(trainer));
    }

    @Transactional
    public void deleteTrainer(String username) {
        log.info("Attempting to delete trainer profile: {}", username);

        Trainer trainer = trainerRepository.findByUsername(username)
                .orElseThrow(() -> new TrainerNotFoundException(TRAINER_NOT_FOUND_MESSAGE));

        List<Training> trainings = trainer.getTrainings() == null
                ? List.of()
                : List.copyOf(trainer.getTrainings());

        for (Training training : trainings) {
            if (training.getTrainee() != null && training.getTrainee().getTrainings() != null) {
                training.getTrainee().getTrainings().remove(training);
            }
        }

        if (!trainings.isEmpty()) {
            trainingRepository.deleteAll(trainings);
        }

        if (trainer.getTrainings() != null) {
            trainer.getTrainings().clear();
        }

        for (Trainee trainee : new HashSet<>(trainer.getTrainees())) {
            trainee.getTrainers().remove(trainer);
        }
        trainer.getTrainees().clear();

        trainerRepository.delete(trainer);
        log.info("Successfully deleted trainer profile: {}", username);
    }
}
