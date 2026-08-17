import epam.arsen.burko.gym.controller.TrainingController;
import epam.arsen.burko.gym.dto.TrainingCreateRequest;
import epam.arsen.burko.gym.service.TrainingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TrainingControllerTest {

    @Mock
    private TrainingService trainingService;

    @InjectMocks
    private TrainingController trainingController;

    @Test
    void addTraining_ValidRequest_ReturnsOk() {
        TrainingCreateRequest request = new TrainingCreateRequest("Jane.Smith", "John.Doe", "Morning Yoga", LocalDate.of(2026, 7, 1), 60);

        ResponseEntity<Void> response = trainingController.addTraining(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(trainingService).addTraining(request);
    }

    @Test
    void deleteTraining_ValidId_DelegatesToService() {
        trainingController.deleteTraining(42L);

        verify(trainingService).deleteTraining(42L);
    }
}

