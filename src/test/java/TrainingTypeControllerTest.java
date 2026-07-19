import epam.arsen.burko.gym.controller.TrainingTypeController;
import epam.arsen.burko.gym.dto.TrainingTypeResponse;
import epam.arsen.burko.gym.service.TrainingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingTypeControllerTest {

    @Mock
    private TrainingService trainingService;

    @InjectMocks
    private TrainingTypeController trainingTypeController;

    @Test
    void getTrainingTypes_ReturnsServiceResult() {
        List<TrainingTypeResponse> expected = List.of(
                new TrainingTypeResponse(1L, "Yoga"),
                new TrainingTypeResponse(2L, "Fitness")
        );
        when(trainingService.getTrainingTypes()).thenReturn(expected);

        List<TrainingTypeResponse> response = trainingTypeController.getTrainingTypes();

        assertEquals(expected, response);
        verify(trainingService).getTrainingTypes();
    }
}

