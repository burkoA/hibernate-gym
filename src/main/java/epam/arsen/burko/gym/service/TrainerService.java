package epam.arsen.burko.gym.service;

import epam.arsen.burko.gym.dto.TrainerDto;
import epam.arsen.burko.gym.dto.TrainerUpdateDto;
import epam.arsen.burko.gym.entity.Trainer;
import epam.arsen.burko.gym.entity.TrainingType;
import epam.arsen.burko.gym.exception.SpecializationNotFoundException;
import epam.arsen.burko.gym.exception.TrainerNotFoundException;
import epam.arsen.burko.gym.repository.TrainerRepository;
import epam.arsen.burko.gym.repository.TrainingTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static epam.arsen.burko.gym.dto.GymDtoMapper.toDto;

@Service
@Slf4j
@RequiredArgsConstructor
public class TrainerService {
    private final TrainerRepository trainerRepository;
    private final IdentityGenerationService identityService;
    private final TrainingTypeRepository trainingTypeRepository;
    private final AuthService auth;


    @Transactional
    public TrainerDto createTrainer(String firstName, String lastName, Long specializationId) {
        log.info("Creating new trainer profile for: {} {}", firstName, lastName);

        TrainingType specialization = trainingTypeRepository.findById(specializationId)
                .orElseThrow(() -> new SpecializationNotFoundException("Specialization not found"));

        Trainer trainer = new Trainer();
        trainer.setFirstName(firstName);
        trainer.setLastName(lastName);
        trainer.setUsername(identityService.generateUsername(firstName, lastName));
        trainer.setPassword(identityService.generatePassword());
        trainer.setIsActive(true);
        trainer.setSpecialization(specialization);

        log.info("Successfully created trainer with username: {}", trainer.getUsername());

        return toDto(trainerRepository.save(trainer));
    }

    public TrainerDto get(String username, String password) {
        log.info("Fetching profile for trainer: {}", username);
        auth.validate(username, password);
        return toDto(trainerRepository.findByUsername(username)
                .orElseThrow(() -> new TrainerNotFoundException("Trainer not found")));
    }

    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword) {
        log.info("Processing password change for trainer: {}", username);
        auth.validate(username, oldPassword);
        Trainer trainer = trainerRepository.findByUsername(username)
                .orElseThrow(() -> new TrainerNotFoundException("Trainer not found"));
        trainer.setPassword(newPassword);
        log.info("Successfully changed password for trainer: {}", username);
    }

    @Transactional
    public void toggleStatus(String username, String password,boolean isActive) {
        log.info("Toggling active status for trainer: {} to {}", username, isActive);
        auth.validate(username, password);

        Trainer trainer = trainerRepository.findByUsername(username)
                .orElseThrow(() -> new TrainerNotFoundException("Trainer not found"));
        trainer.setIsActive(isActive);

        trainerRepository.save(trainer);
        log.info("Successfully updated status for trainer: {}", username);
    }

    @Transactional
    public TrainerDto updateProfile(String username, String password, TrainerUpdateDto updated) {
        log.info("Updating profile for trainer: {}", username);
        auth.validate(username, password);

        Trainer trainer = trainerRepository.findByUsername(username)
                .orElseThrow(() -> new TrainerNotFoundException("Trainer not found"));

        trainer.setFirstName(updated.firstName());
        trainer.setLastName(updated.lastName());

        if (updated.specializationId() != null) {
            TrainingType specialization = trainingTypeRepository.findById(updated.specializationId())
                    .orElseThrow(() -> new SpecializationNotFoundException("Specialization not found"));
            trainer.setSpecialization(specialization);
        }

        log.info("Successfully updated profile for trainer: {}", username);
        return toDto(trainer);
    }
}
