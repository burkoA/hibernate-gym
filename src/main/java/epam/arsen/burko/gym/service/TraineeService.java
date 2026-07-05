package epam.arsen.burko.gym.service;

import epam.arsen.burko.gym.dto.TraineeDto;
import epam.arsen.burko.gym.dto.TraineeUpdateDto;
import epam.arsen.burko.gym.dto.TrainerDto;
import epam.arsen.burko.gym.entity.Trainee;
import epam.arsen.burko.gym.entity.Trainer;
import epam.arsen.burko.gym.exception.TraineeNotFoundException;
import epam.arsen.burko.gym.repository.TraineeRepository;
import epam.arsen.burko.gym.repository.TrainerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static epam.arsen.burko.gym.dto.GymDtoMapper.toDto;

@Service
@Slf4j
@RequiredArgsConstructor
public class TraineeService {
    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final IdentityGenerationService identityService;
    private final AuthService auth;

    @Transactional
    public TraineeDto createTrainee(String firstName, String lastName, LocalDate dateOfBirth, String address) {
        log.info("Creating new trainee profile for: {} {}", firstName, lastName);

        Trainee trainee = new Trainee();
        trainee.setFirstName(firstName);
        trainee.setLastName(lastName);
        trainee.setUsername(identityService.generateUsername(firstName, lastName));
        trainee.setPassword(identityService.generatePassword());
        trainee.setIsActive(true);
        trainee.setDateOfBirth(dateOfBirth);
        trainee.setAddress(address);

        log.info("Successfully created trainee with username: {}", trainee.getUsername());
        return toDto(traineeRepository.save(trainee));
    }

    @Transactional
    public void updateTrainers(String username, String password, Set<Long> trainerIds) {
        log.info("Updating trainer list for trainee: {}", username);
        auth.validate(username, password);
        Trainee trainee = traineeRepository.findByUsername(username)
                .orElseThrow(() -> new TraineeNotFoundException("Trainee not found"));
        trainee.setTrainers(new HashSet<>(trainerRepository.findAllById(trainerIds)));
        log.info("Successfully updated trainer list for trainee: {}", username);
    }

    public TraineeDto get(String username, String password) {
        log.info("Fetching profile for trainee: {}", username);
        auth.validate(username, password);
        return toDto(traineeRepository.findByUsername(username)
                .orElseThrow(() -> new TraineeNotFoundException("Trainee not found")));
    }

    @Transactional
    public TraineeDto updateProfile(String username, String password, TraineeUpdateDto updated) {
        log.info("Updating profile for trainee: {}", username);
        auth.validate(username, password);

        Trainee trainee = traineeRepository.findByUsername(username)
                .orElseThrow(() -> new TraineeNotFoundException("Trainee not found"));

        trainee.setFirstName(updated.firstName());
        trainee.setLastName(updated.lastName());
        trainee.setDateOfBirth(updated.dateOfBirth());
        trainee.setAddress(updated.address());
        log.info("Successfully updated profile for trainee: {}", username);
        return toDto(trainee);
    }

    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword) {
        log.info("Processing password change for trainee: {}", username);
        auth.validate(username, oldPassword);
        Trainee trainee = traineeRepository.findByUsername(username)
                .orElseThrow(() -> new TraineeNotFoundException("Trainee not found"));
        trainee.setPassword(newPassword);
        log.info("Successfully changed password for trainee: {}", username);
    }

    @Transactional
    public void toggleStatus(String username, String password, boolean isActive) {
        log.info("Toggling active status for trainee: {} to {}", username, isActive);
        auth.validate(username, password);

        Trainee trainee = traineeRepository.findByUsername(username)
                .orElseThrow(() -> new TraineeNotFoundException("Trainee not found"));
        trainee.setIsActive(isActive);

        traineeRepository.save(trainee);
        log.info("Successfully updated status for trainee: {}", username);
    }

    @Transactional
    public void deleteTrainee(String username, String password) {
        log.info("Attempting to delete trainee profile: {}", username);
        auth.validate(username, password);
        Trainee trainee = traineeRepository.findByUsername(username)
                .orElseThrow(() -> new TraineeNotFoundException("Trainee not found"));
        traineeRepository.delete(trainee);
        log.info("Successfully deleted trainee profile: {}", username);
    }

    @Transactional
    public List<TrainerDto> getUnassigned(String username, String password) {
        log.info("Fetching unassigned trainers for trainee: {}", username);
        auth.validate(username, password);

        return trainerRepository.findTrainersNotAssignedToTrainee(username).stream()
                .map(trainer -> toDto(trainer))
                .toList();
    }
}
