import epam.arsen.burko.gym.dto.TraineeDto;
import epam.arsen.burko.gym.dto.TraineeProfileResponse;
import epam.arsen.burko.gym.dto.TraineeTrainerListUpdateRequest;
import epam.arsen.burko.gym.dto.TraineeUpdateRequest;
import epam.arsen.burko.gym.dto.TraineeUpdateResponse;
import epam.arsen.burko.gym.dto.TrainerSummaryDto;
import epam.arsen.burko.gym.dto.TrainerUsernameRequest;
import epam.arsen.burko.gym.entity.Trainee;
import epam.arsen.burko.gym.entity.Trainer;
import epam.arsen.burko.gym.entity.TrainingType;
import epam.arsen.burko.gym.exception.RoleConflictException;
import epam.arsen.burko.gym.exception.TraineeNotFoundException;
import epam.arsen.burko.gym.exception.TrainerNotFoundException;
import epam.arsen.burko.gym.repository.TraineeRepository;
import epam.arsen.burko.gym.repository.TrainerRepository;
import epam.arsen.burko.gym.repository.UserRepository;
import epam.arsen.burko.gym.service.IdentityGenerationService;
import epam.arsen.burko.gym.service.TraineeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TraineeServiceTest {

    @Mock
    private TraineeRepository traineeRepository;
    @Mock
    private TrainerRepository trainerRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private IdentityGenerationService identityService;

    @InjectMocks
    private TraineeService traineeService;

    @Test
    void createTrainee_SavesAndReturnsTrainee() {
        when(userRepository.findByUsernameStartingWith("Jane.Smith")).thenReturn(Collections.emptyList());
        when(identityService.generateUsername("Jane", "Smith")).thenReturn("Jane.Smith");
        when(identityService.generatePassword()).thenReturn("randomPass");
        when(traineeRepository.save(any(Trainee.class))).thenAnswer(i -> i.getArguments()[0]);

        TraineeDto result = traineeService.createTrainee("Jane", "Smith", LocalDate.now(), "123 Main St");

        assertEquals("Jane", result.firstName());
        assertEquals("Jane.Smith", result.username());
        assertEquals("randomPass", result.password());
        assertTrue(result.isActive());
        assertEquals("123 Main St", result.address());
        verify(userRepository, times(1)).findByUsernameStartingWith("Jane.Smith");
        verify(traineeRepository, times(1)).save(any(Trainee.class));
    }

    @Test
    void createTrainee_WhenTrainerProfileExists_ThrowsRoleConflictException() {
        Trainer trainer = new Trainer();
        trainer.setUsername("Jane.Smith");
        when(userRepository.findByUsernameStartingWith("Jane.Smith")).thenReturn(List.of(trainer));
        LocalDate today = LocalDate.now();

        RoleConflictException exception = assertThrows(
                RoleConflictException.class,
                () -> traineeService.createTrainee("Jane", "Smith", today, "123 Main St")
        );

        assertEquals("Trainer profile already exists for this user", exception.getMessage());
        verify(userRepository, times(1)).findByUsernameStartingWith("Jane.Smith");
        verifyNoInteractions(identityService);
        verify(traineeRepository, never()).save(any(Trainee.class));
    }

    @Test
    void getProfile_ExistingTrainee_ReturnsProfileResponse() {
        Trainee trainee = new Trainee();
        trainee.setFirstName("Jane");
        trainee.setLastName("Smith");
        trainee.setUsername("Jane.Smith");
        trainee.setIsActive(true);
        trainee.setDateOfBirth(LocalDate.of(1990, 1, 1));
        trainee.setAddress("123 Main St");
        trainee.setTrainers(new HashSet<>());
        when(traineeRepository.findByUsername("Jane.Smith")).thenReturn(Optional.of(trainee));

        TraineeProfileResponse result = traineeService.getProfile("Jane.Smith");

        assertEquals("Jane", result.firstName());
        assertEquals("Smith", result.lastName());
        assertTrue(result.isActive());
        assertEquals(0, result.trainers().size());
    }

    @Test
    void updateProfile_RequestBased_UpdatesFields() {
        Trainee trainee = new Trainee();
        trainee.setUsername("Jane.Smith");
        trainee.setTrainers(new HashSet<>());
        when(traineeRepository.findByUsername("Jane.Smith")).thenReturn(Optional.of(trainee));
        when(traineeRepository.save(trainee)).thenReturn(trainee);

        TraineeUpdateResponse result = traineeService.updateProfile("Jane.Smith", new TraineeUpdateRequest(
                "Jane",
                "Smith",
                LocalDate.of(1990, 1, 1),
                "123 Main St",
                true
        ));

        assertEquals("Jane.Smith", result.username());
        assertEquals("Jane", result.firstName());
        assertTrue(result.isActive());
        verify(traineeRepository, times(1)).save(trainee);
    }

    @Test
    void getUnassignedActiveTrainers_ExistingTrainee_ReturnsTrainerSummaries() {
        Trainee trainee = new Trainee();
        trainee.setUsername("Jane.Smith");

        TrainingType trainingType = new TrainingType();
        trainingType.setId(1L);
        trainingType.setTrainingTypeName("Yoga");

        Trainer trainer = new Trainer();
        trainer.setUsername("John.Doe");
        trainer.setFirstName("John");
        trainer.setLastName("Doe");
        trainer.setSpecialization(trainingType);
        trainer.setIsActive(true);

        when(traineeRepository.findByUsername("Jane.Smith")).thenReturn(Optional.of(trainee));
        when(trainerRepository.findActiveTrainersNotAssignedToTrainee("Jane.Smith")).thenReturn(List.of(trainer));

        List<TrainerSummaryDto> result = traineeService.getUnassignedActiveTrainers("Jane.Smith");

        assertEquals(1, result.size());
        assertEquals("John.Doe", result.get(0).username());
        verify(traineeRepository, times(1)).findByUsername("Jane.Smith");
        verify(trainerRepository, times(1)).findActiveTrainersNotAssignedToTrainee("Jane.Smith");
    }

    @Test
    void updateTrainers_ExistingTrainee_ReturnsUpdatedTrainerSummaries() {
        Trainee trainee = new Trainee();
        trainee.setUsername("Jane.Smith");

        TrainingType trainingType = new TrainingType();
        trainingType.setId(1L);
        trainingType.setTrainingTypeName("Yoga");

        Trainer trainer = new Trainer();
        trainer.setUsername("John.Doe");
        trainer.setFirstName("John");
        trainer.setLastName("Doe");
        trainer.setSpecialization(trainingType);

        TraineeTrainerListUpdateRequest request = new TraineeTrainerListUpdateRequest(
                List.of(new TrainerUsernameRequest("John.Doe"))
        );

        when(traineeRepository.findByUsername("Jane.Smith")).thenReturn(Optional.of(trainee));
        when(trainerRepository.findByUsernameIn(List.of("John.Doe"))).thenReturn(List.of(trainer));
        when(traineeRepository.save(trainee)).thenReturn(trainee);

        List<TrainerSummaryDto> result = traineeService.updateTrainers("Jane.Smith", request);

        assertEquals(1, result.size());
        assertEquals("John.Doe", result.get(0).username());
        assertEquals(1, trainee.getTrainers().size());
        verify(traineeRepository, times(1)).save(trainee);
    }

    @Test
    void updateTrainers_MissingTrainer_ThrowsTrainerNotFoundException() {
        Trainee trainee = new Trainee();
        trainee.setUsername("Jane.Smith");

        TraineeTrainerListUpdateRequest request = new TraineeTrainerListUpdateRequest(
                List.of(new TrainerUsernameRequest("Missing.Trainer"))
        );

        when(traineeRepository.findByUsername("Jane.Smith")).thenReturn(Optional.of(trainee));
        when(trainerRepository.findByUsernameIn(List.of("Missing.Trainer"))).thenReturn(List.of());

        TrainerNotFoundException exception = assertThrows(
                TrainerNotFoundException.class,
                () -> traineeService.updateTrainers("Jane.Smith", request)
        );

        assertEquals("Trainers not found: Missing.Trainer", exception.getMessage());
        verify(traineeRepository, times(1)).findByUsername("Jane.Smith");
        verify(traineeRepository, never()).save(any(Trainee.class));
    }

    @Test
    void updateTrainers_EmptyList_ClearsTrainerAssignments() {
        Trainee trainee = new Trainee();
        trainee.setUsername("Jane.Smith");
        trainee.setTrainers(new HashSet<>(List.of(new Trainer())));

        TraineeTrainerListUpdateRequest request = new TraineeTrainerListUpdateRequest(List.of());

        when(traineeRepository.findByUsername("Jane.Smith")).thenReturn(Optional.of(trainee));
        when(trainerRepository.findByUsernameIn(List.of())).thenReturn(List.of());
        when(traineeRepository.save(trainee)).thenReturn(trainee);

        List<TrainerSummaryDto> result = traineeService.updateTrainers("Jane.Smith", request);

        assertTrue(result.isEmpty());
        assertTrue(trainee.getTrainers().isEmpty());
        verify(traineeRepository, times(1)).save(trainee);
    }

    @Test
    void toggleStatus_ExistingTrainee_UpdatesActiveFlag() {
        Trainee trainee = new Trainee();
        trainee.setUsername("Jane.Smith");
        trainee.setIsActive(true);

        when(traineeRepository.findByUsername("Jane.Smith")).thenReturn(Optional.of(trainee));

        traineeService.toggleStatus("Jane.Smith", false);

        assertFalse(trainee.getIsActive());
        verify(traineeRepository, times(1)).save(trainee);
    }

    @Test
    void toggleStatus_MissingTrainee_ThrowsTraineeNotFoundException() {
        when(traineeRepository.findByUsername("Jane.Smith")).thenReturn(Optional.empty());

        TraineeNotFoundException exception = assertThrows(
                TraineeNotFoundException.class,
                () -> traineeService.toggleStatus("Jane.Smith", false)
        );

        assertEquals("Trainee not found", exception.getMessage());
        verify(traineeRepository, never()).save(any(Trainee.class));
    }

    @Test
    void deleteTrainee_ExistingTrainee_DeletesTrainee() {
        Trainee trainee = new Trainee();
        trainee.setUsername("Jane.Smith");
        when(traineeRepository.findByUsername("Jane.Smith")).thenReturn(Optional.of(trainee));

        traineeService.deleteTrainee("Jane.Smith");

        verify(traineeRepository, times(1)).delete(trainee);
    }

    @Test
    void deleteTrainee_MissingTrainee_ThrowsTraineeNotFoundException() {
        when(traineeRepository.findByUsername("Jane.Smith")).thenReturn(Optional.empty());

        assertThrows(TraineeNotFoundException.class, () -> traineeService.deleteTrainee("Jane.Smith"));
        verify(traineeRepository, never()).delete(any(Trainee.class));
    }
}