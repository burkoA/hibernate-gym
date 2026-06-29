package epam.arsen.burko.gym.service;

import epam.arsen.burko.gym.entity.Trainee;
import epam.arsen.burko.gym.entity.Trainer;
import epam.arsen.burko.gym.repository.TraineeRepository;
import epam.arsen.burko.gym.repository.TrainerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class TraineeService {
    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final IdentityGenerationService identityService;
    private final AuthService auth;

    @Transactional
    public Trainee createTrainee(String firstName, String lastName, Date dateOfBirth, String address) {
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
        return traineeRepository.save(trainee);
    }

    @Transactional
    public void updateTrainers(String username, String password, Set<Long> trainerIds) {
        log.info("Updating trainer list for trainee: {}", username);
        auth.validate(username, password);
        Trainee trainee = traineeRepository.findByUsername(username).orElseThrow();
        trainee.setTrainers(new HashSet<>(trainerRepository.findAllById(trainerIds)));
        log.info("Successfully updated trainer list for trainee: {}", username);
    }

    public Trainee get(String username, String password) {
        log.info("Fetching profile for trainee: {}", username);
        auth.validate(username, password);
        return traineeRepository.findByUsername(username).orElseThrow();
    }

    @Transactional
    public Trainee updateProfile(String username, String password, Trainee updated) {
        log.info("Updating profile for trainee: {}", username);
        auth.validate(username, password);

        Trainee trainee = traineeRepository.findByUsername(username).orElseThrow();

        trainee.setFirstName(updated.getFirstName());
        trainee.setLastName(updated.getLastName());
        trainee.setDateOfBirth(updated.getDateOfBirth());
        trainee.setAddress(updated.getAddress());
        log.info("Successfully updated profile for trainee: {}", username);
        return trainee;
    }

    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword) {
        log.info("Processing password change for trainee: {}", username);
        auth.validate(username, oldPassword);
        Trainee trainee = traineeRepository.findByUsername(username).orElseThrow();
        trainee.setPassword(newPassword);
        log.info("Successfully changed password for trainee: {}", username);
    }

    @Transactional
    public void toggleStatus(String username, String password, boolean isActive) {
        log.info("Toggling active status for trainee: {} to {}", username, isActive);
        auth.validate(username, password);

        Trainee trainee = traineeRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Trainee not found"));
        trainee.setIsActive(isActive);

        traineeRepository.save(trainee);
        log.info("Successfully updated status for trainee: {}", username);
    }

    @Transactional
    public void deleteTrainee(String username, String password) {
        log.info("Attempting to delete trainee profile: {}", username);
        auth.validate(username, password);
        Trainee trainee = traineeRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Trainee not found"));
        traineeRepository.delete(trainee);
        log.info("Successfully deleted trainee profile: {}", username);
    }

    @Transactional
    public List<Trainer> getUnassigned(String username, String password) {
        log.info("Fetching unassigned trainers for trainee: {}", username);
        auth.validate(username, password);

        return trainerRepository.findTrainersNotAssignedToTrainee(username);
    }
}
