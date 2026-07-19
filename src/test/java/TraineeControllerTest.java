import epam.arsen.burko.gym.controller.TraineeController;
import epam.arsen.burko.gym.dto.RegistrationResponse;
import epam.arsen.burko.gym.dto.StatusUpdateRequest;
import epam.arsen.burko.gym.dto.TraineeDto;
import epam.arsen.burko.gym.dto.TraineeProfileResponse;
import epam.arsen.burko.gym.dto.TraineeRegistrationRequest;
import epam.arsen.burko.gym.dto.TraineeTrainerListUpdateRequest;
import epam.arsen.burko.gym.dto.TraineeTrainingResponse;
import epam.arsen.burko.gym.dto.TraineeUpdateRequest;
import epam.arsen.burko.gym.dto.TraineeUpdateResponse;
import epam.arsen.burko.gym.dto.TrainerSummaryDto;
import epam.arsen.burko.gym.dto.TrainerUsernameRequest;
import epam.arsen.burko.gym.service.TraineeService;
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
class TraineeControllerTest {

    @Mock
    private TraineeService traineeService;

    @Mock
    private TrainingService trainingService;

    @InjectMocks
    private TraineeController traineeController;

    @Test
    void register_ValidRequest_ReturnsRegistrationResponse() {
        TraineeRegistrationRequest request = new TraineeRegistrationRequest("Jane", "Smith", LocalDate.of(1990, 1, 1), "Street 1");
        when(traineeService.createTrainee("Jane", "Smith", LocalDate.of(1990, 1, 1), "Street 1"))
                .thenReturn(new TraineeDto(1L, "Jane", "Smith", "Jane.Smith", "pwd", true, LocalDate.of(1990, 1, 1), "Street 1"));

        RegistrationResponse response = traineeController.register(request);

        assertEquals("Jane.Smith", response.username());
        assertEquals("pwd", response.password());
        verify(traineeService).createTrainee("Jane", "Smith", LocalDate.of(1990, 1, 1), "Street 1");
    }

    @Test
    void getProfile_ValidUsername_ReturnsProfile() {
        TraineeProfileResponse expected = new TraineeProfileResponse("Jane", "Smith", null, null, true, List.of());
        when(traineeService.getProfile("Jane.Smith")).thenReturn(expected);

        TraineeProfileResponse response = traineeController.getProfile("Jane.Smith");

        assertEquals(expected, response);
        verify(traineeService).getProfile("Jane.Smith");
    }

    @Test
    void getUnassignedActiveTrainers_ValidUsername_ReturnsSummaries() {
        List<TrainerSummaryDto> expected = List.of(new TrainerSummaryDto("john", "John", "Doe", 1L, "Yoga"));
        when(traineeService.getUnassignedActiveTrainers("Jane.Smith")).thenReturn(expected);

        List<TrainerSummaryDto> response = traineeController.getUnassignedActiveTrainers("Jane.Smith");

        assertEquals(expected, response);
        verify(traineeService).getUnassignedActiveTrainers("Jane.Smith");
    }

    @Test
    void getTrainings_ValidFilters_ReturnsTrainings() {
        LocalDate from = LocalDate.of(2026, 1, 1);
        LocalDate to = LocalDate.of(2026, 1, 31);
        List<TraineeTrainingResponse> expected = List.of(
                new TraineeTrainingResponse("Morning", LocalDate.of(2026, 1, 2), 1L, "Yoga", 60, "John Doe")
        );

        when(trainingService.getTraineeTrainings("Jane.Smith", from, to, "John", "Yoga")).thenReturn(expected);

        List<TraineeTrainingResponse> response = traineeController.getTrainings("Jane.Smith", from, to, "John", "Yoga");

        assertEquals(expected, response);
        verify(trainingService).getTraineeTrainings("Jane.Smith", from, to, "John", "Yoga");
    }

    @Test
    void updateTrainerList_ValidRequest_ReturnsUpdatedList() {
        TraineeTrainerListUpdateRequest request = new TraineeTrainerListUpdateRequest(List.of(new TrainerUsernameRequest("john")));
        List<TrainerSummaryDto> expected = List.of(new TrainerSummaryDto("john", "John", "Doe", 1L, "Yoga"));
        when(traineeService.updateTrainers("Jane.Smith", request)).thenReturn(expected);

        List<TrainerSummaryDto> response = traineeController.updateTrainerList("Jane.Smith", request);

        assertEquals(expected, response);
        verify(traineeService).updateTrainers("Jane.Smith", request);
    }

    @Test
    void updateProfile_ValidRequest_ReturnsUpdateResponse() {
        TraineeUpdateRequest request = new TraineeUpdateRequest("Jane", "Smith", null, "Addr", true);
        TraineeUpdateResponse expected = new TraineeUpdateResponse("Jane.Smith", "Jane", "Smith", null, "Addr", true, List.of());
        when(traineeService.updateProfile("Jane.Smith", request)).thenReturn(expected);

        TraineeUpdateResponse response = traineeController.updateProfile("Jane.Smith", request);

        assertEquals(expected, response);
        verify(traineeService).updateProfile("Jane.Smith", request);
    }

    @Test
    void updateStatus_ValidRequest_DelegatesToService() {
        traineeController.updateStatus("Jane.Smith", new StatusUpdateRequest(false));

        verify(traineeService).toggleStatus("Jane.Smith", false);
    }

    @Test
    void deleteProfile_ValidUsername_DelegatesToService() {
        traineeController.deleteProfile("Jane.Smith");

        verify(traineeService).deleteTrainee("Jane.Smith");
    }
}

