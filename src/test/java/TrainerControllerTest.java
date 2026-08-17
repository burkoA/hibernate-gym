import epam.arsen.burko.gym.controller.TrainerController;
import epam.arsen.burko.gym.dto.RegistrationResponse;
import epam.arsen.burko.gym.dto.StatusUpdateRequest;
import epam.arsen.burko.gym.dto.TrainerDto;
import epam.arsen.burko.gym.dto.TrainerProfileResponse;
import epam.arsen.burko.gym.dto.TrainerRegistrationRequest;
import epam.arsen.burko.gym.dto.TrainerTrainingResponse;
import epam.arsen.burko.gym.dto.TrainerUpdateRequest;
import epam.arsen.burko.gym.service.TrainerService;
import epam.arsen.burko.gym.service.TrainingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainerControllerTest {

    @Mock
    private TrainerService trainerService;

    @Mock
    private TrainingService trainingService;

    @InjectMocks
    private TrainerController trainerController;

    @Test
    void register_ValidRequest_ReturnsRegistrationResponse() {
        TrainerRegistrationRequest request = new TrainerRegistrationRequest("John", "Doe", 1L);
        when(trainerService.createTrainer("John", "Doe", 1L))
                .thenReturn(new TrainerDto(1L, "John", "Doe", "John.Doe", "pwd", true, 1L, "Yoga"));

        RegistrationResponse response = trainerController.register(request);

        assertEquals("John.Doe", response.username());
        assertEquals("pwd", response.password());
        verify(trainerService).createTrainer("John", "Doe", 1L);
    }

    @Test
    void getProfile_ValidUsername_ReturnsProfile() {
        TrainerProfileResponse expected = new TrainerProfileResponse("John", "Doe", 1L, "Yoga", true, List.of());
        when(trainerService.getProfile("John.Doe")).thenReturn(expected);

        TrainerProfileResponse response = trainerController.getProfile("John.Doe");

        assertEquals(expected, response);
        verify(trainerService).getProfile("John.Doe");
    }

    @Test
    void getTrainings_ValidFilters_ReturnsTrainingResponses() {
        LocalDate from = LocalDate.of(2026, 1, 1);
        LocalDate to = LocalDate.of(2026, 1, 31);
        List<TrainerTrainingResponse> expected = List.of(
                new TrainerTrainingResponse("Morning", LocalDate.of(2026, 1, 2), 1L, "Yoga", 60, "Jane Smith")
        );
        when(trainingService.getTrainerTrainings("John.Doe", from, to, "Jane")).thenReturn(expected);

        List<TrainerTrainingResponse> response = trainerController.getTrainings("John.Doe", from, to, "Jane");

        assertEquals(expected, response);
        verify(trainingService).getTrainerTrainings("John.Doe", from, to, "Jane");
    }

    @Test
    void updateProfile_ValidRequest_ReturnsUpdatedProfile() {
        TrainerUpdateRequest request = new TrainerUpdateRequest("John", "Doe", false);
        TrainerProfileResponse expected = new TrainerProfileResponse("John", "Doe", 1L, "Yoga", false, List.of());
        when(trainerService.updateProfile("John.Doe", request)).thenReturn(expected);

        TrainerProfileResponse response = trainerController.updateProfile("John.Doe", request);

        assertEquals(expected, response);
        verify(trainerService).updateProfile("John.Doe", request);
    }

    @Test
    void updateStatus_ValidRequest_DelegatesToService() {
        trainerController.updateStatus("John.Doe", new StatusUpdateRequest(false));

        verify(trainerService).toggleStatus("John.Doe", false);
    }

    @Test
    void deleteProfile_ValidUsername_DelegatesToService() {
        trainerController.deleteProfile("John.Doe");

        verify(trainerService).deleteTrainer("John.Doe");
    }
}

