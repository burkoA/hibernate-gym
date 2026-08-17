import epam.arsen.burko.gym.dto.TrainerDto;
import epam.arsen.burko.gym.dto.TrainerProfileResponse;
import epam.arsen.burko.gym.dto.TrainerUpdateRequest;
import epam.arsen.burko.gym.entity.Trainer;
import epam.arsen.burko.gym.entity.Trainee;
import epam.arsen.burko.gym.entity.Training;
import epam.arsen.burko.gym.entity.TrainingType;
import epam.arsen.burko.gym.exception.RoleConflictException;
import epam.arsen.burko.gym.exception.SpecializationNotFoundException;
import epam.arsen.burko.gym.exception.TrainerNotFoundException;
import epam.arsen.burko.gym.repository.TrainerRepository;
import epam.arsen.burko.gym.repository.TrainingRepository;
import epam.arsen.burko.gym.repository.TrainingTypeRepository;
import epam.arsen.burko.gym.repository.UserRepository;
import epam.arsen.burko.gym.service.IdentityGenerationService;
import epam.arsen.burko.gym.service.TrainerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainerServiceTest {

    @Mock
    private TrainerRepository trainerRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private IdentityGenerationService identityService;
    @Mock
    private TrainingTypeRepository trainingTypeRepository;
    @Mock
    private TrainingRepository trainingRepository;

    @InjectMocks
    private TrainerService trainerService;

    @Test
    void createTrainer_SavesAndReturnsTrainer() {
        when(userRepository.findByUsernameStartingWith("John.Doe")).thenReturn(Collections.emptyList());
        TrainingType trainingType = new TrainingType();
        trainingType.setId(1L);
        trainingType.setTrainingTypeName("Yoga");
        when(trainingTypeRepository.findById(1L)).thenReturn(Optional.of(trainingType));
        when(identityService.generateUsername("John", "Doe")).thenReturn("John.Doe");
        when(identityService.generatePassword()).thenReturn("randomPass");
        when(identityService.encodePassword("randomPass")).thenReturn("hashedRandomPass");
        when(trainerRepository.save(any(Trainer.class))).thenAnswer(i -> i.getArguments()[0]);

        TrainerDto result = trainerService.createTrainer("John", "Doe", 1L);

        assertEquals("John", result.firstName());
        assertEquals("John.Doe", result.username());
        assertEquals("randomPass", result.password());
        assertEquals(true, result.isActive());
        assertEquals(1L, result.specializationId());
        assertEquals("Yoga", result.specializationName());
        verify(userRepository, times(1)).findByUsernameStartingWith("John.Doe");
        verify(trainingTypeRepository, times(1)).findById(1L);
        verify(trainerRepository, times(1)).save(any(Trainer.class));
    }

    @Test
    void createTrainer_WhenTraineeProfileExists_ThrowsRoleConflictException() {
        Trainee trainee = new Trainee();
        trainee.setUsername("John.Doe");
        when(userRepository.findByUsernameStartingWith("John.Doe")).thenReturn(List.of(trainee));

        RoleConflictException exception = assertThrows(
                RoleConflictException.class,
                () -> trainerService.createTrainer("John", "Doe", 1L)
        );

        assertEquals("Trainee profile already exists for this user", exception.getMessage());
        verify(userRepository, times(1)).findByUsernameStartingWith("John.Doe");
        verifyNoInteractions(identityService, trainingTypeRepository);
        verify(trainerRepository, never()).save(any(Trainer.class));
    }

    @Test
    void createTrainer_MissingSpecialization_ThrowsSpecializationNotFoundException() {
        when(userRepository.findByUsernameStartingWith("John.Doe")).thenReturn(Collections.emptyList());
        when(trainingTypeRepository.findById(999L)).thenReturn(Optional.empty());

        SpecializationNotFoundException exception = assertThrows(
                SpecializationNotFoundException.class,
                () -> trainerService.createTrainer("John", "Doe", 999L)
        );

        assertEquals("Specialization not found", exception.getMessage());
        verify(trainingTypeRepository, times(1)).findById(999L);
        verify(trainerRepository, never()).save(any(Trainer.class));
    }

    @Test
    void getProfile_ExistingTrainer_ReturnsProfileResponse() {
        TrainingType trainingType = new TrainingType();
        trainingType.setId(1L);
        trainingType.setTrainingTypeName("Yoga");

        Trainer trainer = new Trainer();
        trainer.setUsername("John.Doe");
        trainer.setFirstName("John");
        trainer.setLastName("Doe");
        trainer.setIsActive(true);
        trainer.setSpecialization(trainingType);
        when(trainerRepository.findByUsername("John.Doe")).thenReturn(Optional.of(trainer));

        TrainerProfileResponse result = trainerService.getProfile("John.Doe");

        assertEquals("John", result.firstName());
        assertEquals("Doe", result.lastName());
        assertEquals(true, result.isActive());
        assertEquals(1L, result.specializationId());
    }

    @Test
    void updateProfile_RequestBased_UpdatesFields() {
        TrainingType trainingType = new TrainingType();
        trainingType.setId(1L);
        trainingType.setTrainingTypeName("Yoga");

        Trainer trainer = new Trainer();
        trainer.setUsername("John.Doe");
        trainer.setSpecialization(trainingType);
        when(trainerRepository.findByUsername("John.Doe")).thenReturn(Optional.of(trainer));
        when(trainerRepository.save(trainer)).thenReturn(trainer);

        TrainerProfileResponse result = trainerService.updateProfile("John.Doe", new TrainerUpdateRequest(
                "John",
                "Doe",
                false
        ));

        assertEquals("John", result.firstName());
        assertFalse(result.isActive());
        verify(trainerRepository, times(1)).save(trainer);
    }

    @Test
    void toggleStatus_ExistingTrainer_UpdatesActiveFlag() {
        Trainer trainer = new Trainer();
        trainer.setUsername("John.Doe");
        trainer.setIsActive(true);

        when(trainerRepository.findByUsername("John.Doe")).thenReturn(Optional.of(trainer));

        trainerService.toggleStatus("John.Doe", false);

        assertFalse(trainer.getIsActive());
        verify(trainerRepository, times(1)).findByUsername("John.Doe");
        verify(trainerRepository, times(1)).save(trainer);
    }

    @Test
    void toggleStatus_MissingTrainer_ThrowsTrainerNotFoundException() {
        when(trainerRepository.findByUsername("John.Doe")).thenReturn(Optional.empty());

        TrainerNotFoundException exception = assertThrows(
                TrainerNotFoundException.class,
                () -> trainerService.toggleStatus("John.Doe", false)
        );

        assertEquals("Trainer not found", exception.getMessage());
        verify(trainerRepository, times(1)).findByUsername("John.Doe");
        verify(trainerRepository, never()).save(any(Trainer.class));
    }

    @Test
    void updateProfile_MissingTrainer_ThrowsTrainerNotFoundException() {
        when(trainerRepository.findByUsername("John.Doe")).thenReturn(Optional.empty());

        TrainerNotFoundException exception = assertThrows(
                TrainerNotFoundException.class,
                () -> trainerService.updateProfile("John.Doe", new TrainerUpdateRequest("John", "Doe", true))
        );

        assertEquals("Trainer not found", exception.getMessage());
        verify(trainerRepository, never()).save(any(Trainer.class));
    }

    @Test
    void deleteTrainer_ExistingTrainer_DeletesTrainingsAndUnlinksTrainees() {
        Trainer trainer = new Trainer();
        trainer.setUsername("John.Doe");

        Trainee trainee = new Trainee();
        trainee.setUsername("Jane.Smith");
        trainee.setTrainers(new HashSet<>(List.of(trainer)));

        Training training = new Training();
        training.setTrainer(trainer);
        training.setTrainee(trainee);

        trainer.setTrainees(new HashSet<>(List.of(trainee)));
        trainer.setTrainings(new java.util.ArrayList<>(List.of(training)));
        trainee.setTrainings(new java.util.ArrayList<>(List.of(training)));

        when(trainerRepository.findByUsername("John.Doe")).thenReturn(Optional.of(trainer));

        trainerService.deleteTrainer("John.Doe");

        assertFalse(trainee.getTrainers().contains(trainer));
        assertFalse(trainee.getTrainings().contains(training));
        assertEquals(0, trainer.getTrainees().size());
        assertEquals(0, trainer.getTrainings().size());
        verify(trainingRepository).deleteAll(List.of(training));
        verify(trainerRepository).delete(trainer);
    }

    @Test
    void deleteTrainer_MissingTrainer_ThrowsTrainerNotFoundException() {
        when(trainerRepository.findByUsername("John.Doe")).thenReturn(Optional.empty());

        TrainerNotFoundException exception = assertThrows(
                TrainerNotFoundException.class,
                () -> trainerService.deleteTrainer("John.Doe")
        );

        assertEquals("Trainer not found", exception.getMessage());
        verify(trainingRepository, never()).deleteAll(any());
        verify(trainerRepository, never()).delete(any(Trainer.class));
    }
}
