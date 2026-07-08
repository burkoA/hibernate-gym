import epam.arsen.burko.gym.dto.TraineeTrainingResponse;
import epam.arsen.burko.gym.dto.TrainingCreateRequest;
import epam.arsen.burko.gym.dto.TrainerTrainingResponse;
import epam.arsen.burko.gym.dto.TrainingTypeResponse;
import epam.arsen.burko.gym.entity.Trainee;
import epam.arsen.burko.gym.entity.Trainer;
import epam.arsen.burko.gym.entity.Training;
import epam.arsen.burko.gym.entity.TrainingType;
import epam.arsen.burko.gym.exception.SpecializationNotFoundException;
import epam.arsen.burko.gym.exception.TraineeNotFoundException;
import epam.arsen.burko.gym.exception.TrainerNotFoundException;
import epam.arsen.burko.gym.exception.TrainingTypeNotFoundException;
import epam.arsen.burko.gym.repository.TraineeRepository;
import epam.arsen.burko.gym.repository.TrainerRepository;
import epam.arsen.burko.gym.repository.TrainingRepository;
import epam.arsen.burko.gym.repository.TrainingTypeRepository;
import epam.arsen.burko.gym.service.AuthService;
import epam.arsen.burko.gym.service.TrainingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingServiceTest {

    @Mock
    private TrainingRepository trainingRepository;
    @Mock
    private TraineeRepository traineeRepository;
    @Mock
    private TrainerRepository trainerRepository;
    @Mock
    private TrainingTypeRepository trainingTypeRepository;
    @Mock
    private AuthService authService;

    @InjectMocks
    private TrainingService trainingService;

    @Test
    void getTraineeTrainings_ExistingTrainee_ReturnsFilteredTrainingResponses() {
        Trainee trainee = new Trainee();
        trainee.setUsername("Jane.Smith");

        Trainer trainer = new Trainer();
        trainer.setFirstName("John");
        trainer.setLastName("Doe");

        TrainingType trainingType = new TrainingType();
        trainingType.setId(1L);
        trainingType.setTrainingTypeName("Yoga");

        Training training = new Training();
        training.setTrainee(trainee);
        training.setTrainer(trainer);
        training.setTrainingName("Morning Yoga");
        training.setTrainingType(trainingType);
        training.setTrainingDate(LocalDate.of(2026, 7, 1));
        training.setTrainingDuration(60);

        LocalDate fromDate = LocalDate.of(2026, 7, 1);
        LocalDate toDate = LocalDate.of(2026, 7, 31);

        when(traineeRepository.findByUsername("Jane.Smith")).thenReturn(Optional.of(trainee));
        when(trainingRepository.findTraineeTrainingsByCriteria("Jane.Smith", fromDate, toDate, "John", "Yoga"))
                .thenReturn(List.of(training));

        List<TraineeTrainingResponse> result = trainingService.getTraineeTrainings(
                "Jane.Smith", fromDate, toDate, "John", "Yoga"
        );

        assertEquals(1, result.size());
        assertEquals("Morning Yoga", result.get(0).trainingName());
        assertEquals(LocalDate.of(2026, 7, 1), result.get(0).trainingDate());
        assertEquals(1L, result.get(0).trainingTypeId());
        assertEquals("Yoga", result.get(0).trainingTypeName());
        assertEquals(60, result.get(0).trainingDuration());
        assertEquals("John Doe", result.get(0).trainerName());
        verify(traineeRepository, times(1)).findByUsername("Jane.Smith");
        verify(trainingRepository, times(1))
                .findTraineeTrainingsByCriteria("Jane.Smith", fromDate, toDate, "John", "Yoga");
    }

    @Test
    void getTraineeTrainings_MissingTrainee_ThrowsTraineeNotFoundException() {
        when(traineeRepository.findByUsername("Jane.Smith")).thenReturn(Optional.empty());

        TraineeNotFoundException exception = assertThrows(TraineeNotFoundException.class, () -> trainingService.getTraineeTrainings(
                "Jane.Smith", null, null, null, null
        ));

        assertEquals("Trainee not found", exception.getMessage());
        verify(traineeRepository, times(1)).findByUsername("Jane.Smith");
        verify(trainingRepository, never()).findTraineeTrainingsByCriteria("Jane.Smith", null, null, null, null);
    }

    @Test
    void getTrainerTrainings_ExistingTrainer_ReturnsFilteredTrainingResponses() {
        Trainer trainer = new Trainer();
        trainer.setUsername("John.Doe");

        Trainee trainee = new Trainee();
        trainee.setFirstName("Jane");
        trainee.setLastName("Smith");

        TrainingType trainingType = new TrainingType();
        trainingType.setId(1L);
        trainingType.setTrainingTypeName("Yoga");

        Training training = new Training();
        training.setTrainee(trainee);
        training.setTrainer(trainer);
        training.setTrainingName("Morning Yoga");
        training.setTrainingType(trainingType);
        training.setTrainingDate(LocalDate.of(2026, 7, 1));
        training.setTrainingDuration(60);

        LocalDate fromDate = LocalDate.of(2026, 7, 1);
        LocalDate toDate = LocalDate.of(2026, 7, 31);

        when(trainerRepository.findByUsername("John.Doe")).thenReturn(Optional.of(trainer));
        when(trainingRepository.findTrainerTrainingsByCriteria("John.Doe", fromDate, toDate, "Jane"))
                .thenReturn(List.of(training));

        List<TrainerTrainingResponse> result = trainingService.getTrainerTrainings(
                "John.Doe", fromDate, toDate, "Jane"
        );

        assertEquals(1, result.size());
        assertEquals("Morning Yoga", result.get(0).trainingName());
        assertEquals(LocalDate.of(2026, 7, 1), result.get(0).trainingDate());
        assertEquals(1L, result.get(0).trainingTypeId());
        assertEquals("Yoga", result.get(0).trainingTypeName());
        assertEquals(60, result.get(0).trainingDuration());
        assertEquals("Jane Smith", result.get(0).traineeName());
        verify(trainerRepository, times(1)).findByUsername("John.Doe");
        verify(trainingRepository, times(1))
                .findTrainerTrainingsByCriteria("John.Doe", fromDate, toDate, "Jane");
    }

    @Test
    void getTrainerTrainings_MissingTrainer_ThrowsTrainerNotFoundException() {
        when(trainerRepository.findByUsername("John.Doe")).thenReturn(Optional.empty());

        TrainerNotFoundException exception = assertThrows(TrainerNotFoundException.class, () -> trainingService.getTrainerTrainings(
                "John.Doe", null, null, null
        ));

        assertEquals("Trainer not found", exception.getMessage());
        verify(trainerRepository, times(1)).findByUsername("John.Doe");
        verify(trainingRepository, never()).findTrainerTrainingsByCriteria("John.Doe", null, null, null);
    }

    @Test
    void addTraining_ValidRequest_SavesTraining() {
        Trainee trainee = new Trainee();
        trainee.setUsername("Jane.Smith");

        TrainingType trainingType = new TrainingType();
        trainingType.setId(1L);
        trainingType.setTrainingTypeName("Yoga");

        Trainer trainer = new Trainer();
        trainer.setUsername("John.Doe");
        trainer.setSpecialization(trainingType);

        TrainingCreateRequest request = new TrainingCreateRequest(
                "Jane.Smith",
                "John.Doe",
                "Morning Yoga",
                LocalDate.of(2026, 7, 1),
                60
        );

        when(trainerRepository.findByUsername("John.Doe")).thenReturn(Optional.of(trainer));
        when(traineeRepository.findByUsername("Jane.Smith")).thenReturn(Optional.of(trainee));

        trainingService.addTraining(request);

        verify(trainerRepository, times(1)).findByUsername("John.Doe");
        verify(traineeRepository, times(1)).findByUsername("Jane.Smith");
        verify(trainingRepository, times(1)).save(any(Training.class));
    }

    @Test
    void addTraining_TrainerWithoutSpecialization_ThrowsSpecializationNotFoundException() {
        Trainer trainer = new Trainer();
        trainer.setUsername("John.Doe");
        trainer.setSpecialization(null);

        TrainingCreateRequest request = new TrainingCreateRequest(
                "Jane.Smith",
                "John.Doe",
                "Morning Yoga",
                LocalDate.of(2026, 7, 1),
                60
        );

        when(trainerRepository.findByUsername("John.Doe")).thenReturn(Optional.of(trainer));

        SpecializationNotFoundException exception = assertThrows(
                SpecializationNotFoundException.class,
                () -> trainingService.addTraining(request)
        );

        assertEquals("Trainer specialization not found", exception.getMessage());
        verify(trainerRepository, times(1)).findByUsername("John.Doe");
        verify(traineeRepository, never()).findByUsername(any());
        verify(trainingRepository, never()).save(any(Training.class));
    }

    @Test
    void addTraining_MissingTrainer_ThrowsTrainerNotFoundException() {
        TrainingCreateRequest request = new TrainingCreateRequest(
                "Jane.Smith",
                "John.Doe",
                "Morning Yoga",
                LocalDate.of(2026, 7, 1),
                60
        );

        when(trainerRepository.findByUsername("John.Doe")).thenReturn(Optional.empty());

        TrainerNotFoundException exception = assertThrows(
                TrainerNotFoundException.class,
                () -> trainingService.addTraining(request)
        );

        assertEquals("Trainer not found", exception.getMessage());
        verify(trainingRepository, never()).save(any(Training.class));
    }

    @Test
    void addTraining_MissingTrainee_ThrowsTraineeNotFoundException() {
        TrainingType trainingType = new TrainingType();
        trainingType.setId(1L);
        trainingType.setTrainingTypeName("Yoga");

        Trainer trainer = new Trainer();
        trainer.setUsername("John.Doe");
        trainer.setSpecialization(trainingType);

        TrainingCreateRequest request = new TrainingCreateRequest(
                "Jane.Smith",
                "John.Doe",
                "Morning Yoga",
                LocalDate.of(2026, 7, 1),
                60
        );

        when(trainerRepository.findByUsername("John.Doe")).thenReturn(Optional.of(trainer));
        when(traineeRepository.findByUsername("Jane.Smith")).thenReturn(Optional.empty());

        TraineeNotFoundException exception = assertThrows(
                TraineeNotFoundException.class,
                () -> trainingService.addTraining(request)
        );

        assertEquals("Trainee not found", exception.getMessage());
        verify(trainingRepository, never()).save(any(Training.class));
    }

    @Test
    void add_WithAuthAndValidData_ReturnsSavedTrainingDto() {
        Trainee trainee = new Trainee();
        trainee.setUsername("Jane.Smith");

        Trainer trainer = new Trainer();
        trainer.setUsername("John.Doe");

        TrainingType type = new TrainingType();
        type.setId(1L);
        type.setTrainingTypeName("Yoga");

        when(traineeRepository.findByUsername("Jane.Smith")).thenReturn(Optional.of(trainee));
        when(trainerRepository.findByUsername("John.Doe")).thenReturn(Optional.of(trainer));
        when(trainingTypeRepository.findById(1L)).thenReturn(Optional.of(type));
        when(trainingRepository.save(any(Training.class))).thenAnswer(invocation -> {
            Training saved = invocation.getArgument(0);
            saved.setId(100L);
            return saved;
        });

        var result = trainingService.add(
                "authUser",
                "authPass",
                "Jane.Smith",
                "John.Doe",
                "Morning Yoga",
                1L,
                LocalDate.of(2026, 7, 1),
                60
        );

        assertEquals(100L, result.id());
        assertEquals("Jane.Smith", result.traineeUsername());
        assertEquals("John.Doe", result.trainerUsername());
        verify(authService).validate("authUser", "authPass");
    }

    @Test
    void add_WhenTypeMissing_ThrowsTrainingTypeNotFoundException() {
        Trainee trainee = new Trainee();
        trainee.setUsername("Jane.Smith");

        Trainer trainer = new Trainer();
        trainer.setUsername("John.Doe");

        when(traineeRepository.findByUsername("Jane.Smith")).thenReturn(Optional.of(trainee));
        when(trainerRepository.findByUsername("John.Doe")).thenReturn(Optional.of(trainer));
        when(trainingTypeRepository.findById(1L)).thenReturn(Optional.empty());

        TrainingTypeNotFoundException exception = assertThrows(
                TrainingTypeNotFoundException.class,
                () -> trainingService.add("authUser", "authPass", "Jane.Smith", "John.Doe", "Morning Yoga", 1L, LocalDate.of(2026, 7, 1), 60)
        );

        assertEquals("Training type not found", exception.getMessage());
        verify(authService).validate("authUser", "authPass");
        verify(trainingRepository, never()).save(any(Training.class));
    }

    @Test
    void getTraineeTrainings_WithAuth_ReturnsMappedDtos() {
        Trainee trainee = new Trainee();
        trainee.setUsername("Jane.Smith");

        Trainer trainer = new Trainer();
        trainer.setUsername("John.Doe");

        TrainingType type = new TrainingType();
        type.setId(1L);
        type.setTrainingTypeName("Yoga");

        Training training = new Training();
        training.setId(50L);
        training.setTrainee(trainee);
        training.setTrainer(trainer);
        training.setTrainingName("Morning Yoga");
        training.setTrainingType(type);
        training.setTrainingDate(LocalDate.of(2026, 7, 1));
        training.setTrainingDuration(60);

        when(trainingRepository.findTraineeTrainingsByCriteria("Jane.Smith", null, null, null, null))
                .thenReturn(List.of(training));

        var result = trainingService.getTraineeTrainings("Jane.Smith", "pwd", null, null, null, null);

        assertEquals(1, result.size());
        assertEquals("Morning Yoga", result.get(0).trainingName());
        verify(authService).validate("Jane.Smith", "pwd");
    }

    @Test
    void getTrainerTrainings_WithAuth_ReturnsMappedDtos() {
        Trainee trainee = new Trainee();
        trainee.setUsername("Jane.Smith");

        Trainer trainer = new Trainer();
        trainer.setUsername("John.Doe");

        TrainingType type = new TrainingType();
        type.setId(1L);
        type.setTrainingTypeName("Yoga");

        Training training = new Training();
        training.setId(51L);
        training.setTrainee(trainee);
        training.setTrainer(trainer);
        training.setTrainingName("Evening Yoga");
        training.setTrainingType(type);
        training.setTrainingDate(LocalDate.of(2026, 7, 2));
        training.setTrainingDuration(45);

        when(trainingRepository.findTrainerTrainingsByCriteria("John.Doe", null, null, null))
                .thenReturn(List.of(training));

        var result = trainingService.getTrainerTrainings("John.Doe", "pwd", null, null, null);

        assertEquals(1, result.size());
        assertEquals("Evening Yoga", result.get(0).trainingName());
        verify(authService).validate("John.Doe", "pwd");
    }

    @Test
    void getTrainingTypes_ReturnsMappedTrainingTypes() {
        TrainingType yoga = new TrainingType();
        yoga.setId(1L);
        yoga.setTrainingTypeName("Yoga");

        TrainingType fitness = new TrainingType();
        fitness.setId(2L);
        fitness.setTrainingTypeName("Fitness");

        when(trainingTypeRepository.findAll()).thenReturn(List.of(yoga, fitness));

        List<TrainingTypeResponse> result = trainingService.getTrainingTypes();

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).trainingTypeId());
        assertEquals("Yoga", result.get(0).trainingType());
        assertEquals(2L, result.get(1).trainingTypeId());
        assertEquals("Fitness", result.get(1).trainingType());
        verify(trainingTypeRepository, times(1)).findAll();
    }
}


