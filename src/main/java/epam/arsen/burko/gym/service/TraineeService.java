package epam.arsen.burko.gym.service;

import epam.arsen.burko.gym.dto.TraineeDto;
import epam.arsen.burko.gym.dto.TraineeTrainerListUpdateRequest;
import epam.arsen.burko.gym.dto.TraineeUpdateRequest;
import epam.arsen.burko.gym.dto.TraineeUpdateResponse;
import epam.arsen.burko.gym.dto.TraineeProfileResponse;
import epam.arsen.burko.gym.dto.TrainerSummaryDto;
import epam.arsen.burko.gym.dto.TrainerUsernameRequest;
import epam.arsen.burko.gym.entity.Trainee;
import epam.arsen.burko.gym.entity.Trainer;
import epam.arsen.burko.gym.entity.User;
import epam.arsen.burko.gym.exception.TraineeNotFoundException;
import epam.arsen.burko.gym.exception.RoleConflictException;
import epam.arsen.burko.gym.exception.TrainerNotFoundException;
import epam.arsen.burko.gym.repository.TraineeRepository;
import epam.arsen.burko.gym.repository.TrainerRepository;
import epam.arsen.burko.gym.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static epam.arsen.burko.gym.dto.GymDtoMapper.toDto;
import static epam.arsen.burko.gym.dto.GymDtoMapper.toProfileResponse;
import static epam.arsen.burko.gym.dto.GymDtoMapper.toUpdateResponse;

@Service
@Slf4j
@RequiredArgsConstructor
public class TraineeService {
    private static final String TRAINEE_NOT_FOUND_MESSAGE = "Trainee not found";

    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final UserRepository userRepository;
    private final IdentityGenerationService identityService;

    @Transactional
    public TraineeDto createTrainee(String firstName, String lastName, LocalDate dateOfBirth, String address) {
        log.info("Creating new trainee profile for: {} {}", firstName, lastName);

        String baseUsername = firstName + "." + lastName;
        validateNoTrainerExists(baseUsername);

        Trainee trainee = new Trainee();
        trainee.setFirstName(firstName);
        trainee.setLastName(lastName);
        trainee.setUsername(identityService.generateUsername(firstName, lastName));
        String plainPassword = identityService.generatePassword();
        trainee.setPassword(identityService.encodePassword(plainPassword));
        trainee.setIsActive(true);
        trainee.setDateOfBirth(dateOfBirth);
        trainee.setAddress(address);

        Trainee savedTrainee = traineeRepository.save(trainee);
        log.info("Successfully created trainee with username: {}", trainee.getUsername());
        
        return new TraineeDto(
                savedTrainee.getId(),
                savedTrainee.getFirstName(),
                savedTrainee.getLastName(),
                savedTrainee.getUsername(),
                plainPassword,
                savedTrainee.getIsActive(),
                savedTrainee.getDateOfBirth(),
                savedTrainee.getAddress()
        );
    }

    private void validateNoTrainerExists(String baseUsername) {
        List<User> existingUsers = userRepository.findByUsernameStartingWith(baseUsername);
        boolean trainerExists = existingUsers.stream().anyMatch(Trainer.class::isInstance);
        if (trainerExists) {
            throw new RoleConflictException("Trainer profile already exists for this user");
        }
    }

    public TraineeProfileResponse getProfile(String username) {
        log.info("Fetching public profile for trainee: {}", username);
        return toProfileResponse(traineeRepository.findByUsername(username)
                .orElseThrow(() -> new TraineeNotFoundException(TRAINEE_NOT_FOUND_MESSAGE)));
    }

    @Transactional(readOnly = true)
    public List<TrainerSummaryDto> getUnassignedActiveTrainers(String username) {
        log.info("Fetching unassigned active trainers for trainee: {}", username);

        traineeRepository.findByUsername(username)
                .orElseThrow(() -> new TraineeNotFoundException(TRAINEE_NOT_FOUND_MESSAGE));

        return trainerRepository.findActiveTrainersNotAssignedToTrainee(username).stream()
                .map(trainer -> new TrainerSummaryDto(
                        trainer.getUsername(),
                        trainer.getFirstName(),
                        trainer.getLastName(),
                        trainer.getSpecialization() != null ? trainer.getSpecialization().getId() : null,
                        trainer.getSpecialization() != null ? trainer.getSpecialization().getTrainingTypeName() : null
                ))
                .toList();
    }

    @Transactional
    public List<TrainerSummaryDto> updateTrainers(String username, TraineeTrainerListUpdateRequest request) {
        log.info("Updating trainer list for trainee: {}", username);

        Trainee trainee = traineeRepository.findByUsername(username)
                .orElseThrow(() -> new TraineeNotFoundException(TRAINEE_NOT_FOUND_MESSAGE));

        List<String> trainerUsernames = request.trainers().stream()
                .map(TrainerUsernameRequest::username)
                .toList();

        List<Trainer> trainers = trainerRepository.findByUsernameIn(trainerUsernames);
        Map<String, Trainer> trainersByUsername = trainers.stream()
                .collect(Collectors.toMap(Trainer::getUsername, trainer -> trainer, (first, second) -> first, HashMap::new));

        List<String> missingUsernames = trainerUsernames.stream()
                .filter(requestedUsername -> !trainersByUsername.containsKey(requestedUsername))
                .distinct()
                .toList();

        if (!missingUsernames.isEmpty()) {
            throw new TrainerNotFoundException("Trainers not found: " + String.join(", ", missingUsernames));
        }

        List<Trainer> orderedTrainers = trainerUsernames.stream()
                .distinct()
                .map(trainersByUsername::get)
                .toList();

        trainee.setTrainers(new HashSet<>(orderedTrainers));
        traineeRepository.save(trainee);

        log.info("Successfully updated trainer list for trainee: {}", username);
        return orderedTrainers.stream()
                .map(trainer -> new TrainerSummaryDto(
                        trainer.getUsername(),
                        trainer.getFirstName(),
                        trainer.getLastName(),
                        trainer.getSpecialization() != null ? trainer.getSpecialization().getId() : null,
                        trainer.getSpecialization() != null ? trainer.getSpecialization().getTrainingTypeName() : null
                ))
                .toList();
    }

    @Transactional
    public TraineeUpdateResponse updateProfile(String username, TraineeUpdateRequest request) {
        log.info("Updating profile for trainee: {}", username);
        Trainee trainee = traineeRepository.findByUsername(username)
                .orElseThrow(() -> new TraineeNotFoundException(TRAINEE_NOT_FOUND_MESSAGE));

        trainee.setFirstName(request.firstName());
        trainee.setLastName(request.lastName());
        trainee.setDateOfBirth(request.dateOfBirth());
        trainee.setAddress(request.address());
        trainee.setIsActive(request.isActive());

        log.info("Successfully updated profile for trainee: {}", username);
        return toUpdateResponse(traineeRepository.save(trainee));
    }

    @Transactional
    public void toggleStatus(String username, boolean isActive) {
        log.info("Toggling active status for trainee: {} to {}", username, isActive);

        Trainee trainee = traineeRepository.findByUsername(username)
                .orElseThrow(() -> new TraineeNotFoundException(TRAINEE_NOT_FOUND_MESSAGE));
        trainee.setIsActive(isActive);

        traineeRepository.save(trainee);
        log.info("Successfully updated status for trainee: {}", username);
    }

    @Transactional
    public void deleteTrainee(String username) {
        log.info("Attempting to delete trainee profile: {}", username);
        Trainee trainee = traineeRepository.findByUsername(username)
                .orElseThrow(() -> new TraineeNotFoundException(TRAINEE_NOT_FOUND_MESSAGE));
        traineeRepository.delete(trainee);
        log.info("Successfully deleted trainee profile: {}", username);
    }
}
