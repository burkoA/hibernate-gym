package epam.arsen.burko.gym.service;

import epam.arsen.burko.gym.entity.Trainer;
import epam.arsen.burko.gym.entity.TrainingType;
import epam.arsen.burko.gym.repository.TrainerRepository;
import epam.arsen.burko.gym.repository.TrainingTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class TrainerService {
    private final TrainerRepository trainerRepository;
    private final IdentityGenerationService identityService;
    private final TrainingTypeRepository trainingTypeRepository;
    private final AuthService auth;


    @Transactional
    public Trainer createTrainer(String firstName, String lastName, Long specializationId) {
        log.info("Creating new trainer profile for: {} {}", firstName, lastName);

        TrainingType specialization = trainingTypeRepository.findById(specializationId)
                .orElseThrow(() -> new RuntimeException("Specialization not found"));

        Trainer trainer = new Trainer();
        trainer.setFirstName(firstName);
        trainer.setLastName(lastName);
        trainer.setUsername(identityService.generateUsername(firstName, lastName));
        trainer.setPassword(identityService.generatePassword());
        trainer.setIsActive(true);
        trainer.setSpecialization(specialization);

        log.info("Successfully created trainer with username: {}", trainer.getUsername());

        return trainerRepository.save(trainer);
    }

    public Trainer get(String username, String password) {
        log.info("Fetching profile for trainer: {}", username);
        auth.validate(username, password);
        return trainerRepository.findByUsername(username).orElseThrow();
    }

    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword) {
        log.info("Processing password change for trainer: {}", username);
        auth.validate(username, oldPassword);
        Trainer trainer = trainerRepository.findByUsername(username).orElseThrow();
        trainer.setPassword(newPassword);
        log.info("Successfully changed password for trainer: {}", username);
    }

    @Transactional
    public void toggleStatus(String username, String password,boolean isActive) {
        log.info("Toggling active status for trainer: {} to {}", username, isActive);
        auth.validate(username, password);

        Trainer trainer = trainerRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Trainer not found"));
        trainer.setIsActive(isActive);

        trainerRepository.save(trainer);
        log.info("Successfully updated status for trainer: {}", username);
    }

    @Transactional
    public Trainer updateProfile(String username, String password, Trainer updated) {
        log.info("Updating profile for trainer: {}", username);
        auth.validate(username, password);

        Trainer trainer = trainerRepository.findByUsername(username).orElseThrow();

        trainer.setFirstName(updated.getFirstName());
        trainer.setLastName(updated.getLastName());
        trainer.setSpecialization(updated.getSpecialization());

        log.info("Successfully updated profile for trainer: {}", username);
        return trainer;
    }
}
