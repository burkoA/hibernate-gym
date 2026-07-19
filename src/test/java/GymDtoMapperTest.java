import epam.arsen.burko.gym.dto.GymDtoMapper;
import epam.arsen.burko.gym.dto.TraineeDto;
import epam.arsen.burko.gym.dto.TraineeProfileResponse;
import epam.arsen.burko.gym.dto.TraineeUpdateResponse;
import epam.arsen.burko.gym.dto.TrainerDto;
import epam.arsen.burko.gym.dto.TrainerProfileResponse;
import epam.arsen.burko.gym.dto.TrainingDto;
import epam.arsen.burko.gym.entity.Trainee;
import epam.arsen.burko.gym.entity.Trainer;
import epam.arsen.burko.gym.entity.Training;
import epam.arsen.burko.gym.entity.TrainingType;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class GymDtoMapperTest {

    @Test
    void constructor_InvokedViaReflection_ForCoverage() throws Exception {
        Constructor<GymDtoMapper> constructor = GymDtoMapper.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        GymDtoMapper mapper = constructor.newInstance();

        assertNotNull(mapper);
    }

    @Test
    void toDto_Trainee_MapsAllFields() {
        Trainee trainee = new Trainee();
        trainee.setId(1L);
        trainee.setFirstName("Jane");
        trainee.setLastName("Smith");
        trainee.setUsername("Jane.Smith");
        trainee.setPassword("pwd");
        trainee.setIsActive(true);
        trainee.setDateOfBirth(LocalDate.of(1990, 1, 1));
        trainee.setAddress("Street 1");

        TraineeDto dto = GymDtoMapper.toDto(trainee);

        assertEquals(1L, dto.id());
        assertEquals("Jane.Smith", dto.username());
        assertEquals("Street 1", dto.address());
    }

    @Test
    void toProfileResponse_Trainee_MapsTrainersWithAndWithoutSpecialization() {
        TrainingType type = new TrainingType();
        type.setId(11L);
        type.setTrainingTypeName("Yoga");

        Trainer withType = new Trainer();
        withType.setUsername("john");
        withType.setFirstName("John");
        withType.setLastName("Doe");
        withType.setSpecialization(type);

        Trainer withoutType = new Trainer();
        withoutType.setUsername("no.spec");
        withoutType.setFirstName("No");
        withoutType.setLastName("Spec");

        Trainee trainee = new Trainee();
        trainee.setFirstName("Jane");
        trainee.setLastName("Smith");
        trainee.setDateOfBirth(LocalDate.of(1990, 1, 1));
        trainee.setAddress("Street 1");
        trainee.setIsActive(true);
        trainee.setTrainers(Set.of(withType, withoutType));

        TraineeProfileResponse response = GymDtoMapper.toProfileResponse(trainee);

        assertEquals("Jane", response.firstName());
        assertEquals(2, response.trainers().size());
    }

    @Test
    void toUpdateResponse_Trainee_MapsUserAndTrainers() {
        Trainer trainer = new Trainer();
        trainer.setUsername("john");
        trainer.setFirstName("John");
        trainer.setLastName("Doe");

        Trainee trainee = new Trainee();
        trainee.setUsername("jane");
        trainee.setFirstName("Jane");
        trainee.setLastName("Smith");
        trainee.setDateOfBirth(LocalDate.of(1990, 1, 1));
        trainee.setAddress("Street 1");
        trainee.setIsActive(true);
        trainee.setTrainers(Set.of(trainer));

        TraineeUpdateResponse response = GymDtoMapper.toUpdateResponse(trainee);

        assertEquals("jane", response.username());
        assertEquals(1, response.trainers().size());
        assertEquals("john", response.trainers().get(0).username());
    }

    @Test
    void toProfileResponse_Trainer_MapsTraineesAndSpecialization() {
        TrainingType type = new TrainingType();
        type.setId(7L);
        type.setTrainingTypeName("Fitness");

        Trainee trainee = new Trainee();
        trainee.setUsername("jane");
        trainee.setFirstName("Jane");
        trainee.setLastName("Smith");

        Trainer trainer = new Trainer();
        trainer.setFirstName("John");
        trainer.setLastName("Doe");
        trainer.setIsActive(true);
        trainer.setSpecialization(type);
        trainer.setTrainees(Set.of(trainee));

        TrainerProfileResponse response = GymDtoMapper.toProfileResponse(trainer);

        assertEquals("John", response.firstName());
        assertEquals(7L, response.specializationId());
        assertEquals(1, response.trainees().size());
    }

    @Test
    void toDto_Trainer_WithNullSpecialization_SetsNullSpecializationFields() {
        Trainer trainer = new Trainer();
        trainer.setId(2L);
        trainer.setFirstName("John");
        trainer.setLastName("Doe");
        trainer.setUsername("john");
        trainer.setPassword("pwd");
        trainer.setIsActive(true);
        trainer.setSpecialization(null);

        TrainerDto dto = GymDtoMapper.toDto(trainer);

        assertEquals(2L, dto.id());
        assertEquals("john", dto.username());
        assertNull(dto.specializationId());
        assertNull(dto.specializationName());
    }

    @Test
    void toDto_Training_MapsNestedFields() {
        TrainingType type = new TrainingType();
        type.setId(3L);
        type.setTrainingTypeName("Boxing");

        Trainee trainee = new Trainee();
        trainee.setUsername("jane");

        Trainer trainer = new Trainer();
        trainer.setUsername("john");

        Training training = new Training();
        training.setId(9L);
        training.setTrainee(trainee);
        training.setTrainer(trainer);
        training.setTrainingName("Evening");
        training.setTrainingType(type);
        training.setTrainingDate(LocalDate.of(2026, 7, 1));
        training.setTrainingDuration(45);

        TrainingDto dto = GymDtoMapper.toDto(training);

        assertEquals(9L, dto.id());
        assertEquals("jane", dto.traineeUsername());
        assertEquals("john", dto.trainerUsername());
        assertEquals("Boxing", dto.trainingTypeName());
    }
}


