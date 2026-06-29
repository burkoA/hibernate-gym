import epam.arsen.burko.gym.entity.Trainee;
import epam.arsen.burko.gym.repository.TraineeRepository;
import epam.arsen.burko.gym.repository.TrainerRepository;
import epam.arsen.burko.gym.service.AuthService;
import epam.arsen.burko.gym.service.IdentityGenerationService;
import epam.arsen.burko.gym.service.TraineeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraineeServiceTest {

    @Mock
    private TraineeRepository traineeRepository;
    @Mock
    private TrainerRepository trainerRepository;
    @Mock
    private IdentityGenerationService identityService;
    @Mock
    private AuthService authService;

    @InjectMocks
    private TraineeService traineeService;

    @Test
    void createTrainee_SavesAndReturnsTrainee() {
        when(identityService.generateUsername("Jane", "Smith")).thenReturn("Jane.Smith");
        when(identityService.generatePassword()).thenReturn("randomPass");
        when(traineeRepository.save(any(Trainee.class))).thenAnswer(i -> i.getArguments()[0]);

        Trainee result = traineeService.createTrainee("Jane", "Smith", new Date(), "123 Main St");

        assertEquals("Jane", result.getFirstName());
        assertEquals("Jane.Smith", result.getUsername());
        assertEquals("randomPass", result.getPassword());
        assertTrue(result.getIsActive());
        assertEquals("123 Main St", result.getAddress());
        verify(traineeRepository, times(1)).save(any(Trainee.class));
    }

    @Test
    void get_ValidCredentials_ReturnsTrainee() {
        Trainee expectedTrainee = new Trainee();
        expectedTrainee.setUsername("Jane.Smith");

        when(traineeRepository.findByUsername("Jane.Smith")).thenReturn(Optional.of(expectedTrainee));

        Trainee result = traineeService.get("Jane.Smith", "password123");

        assertNotNull(result);
        assertEquals("Jane.Smith", result.getUsername());
        verify(authService, times(1)).validate("Jane.Smith", "password123");
    }

    @Test
    void deleteTrainee_ValidCredentials_DeletesTrainee() {
        Trainee trainee = new Trainee();
        trainee.setUsername("Jane.Smith");
        when(traineeRepository.findByUsername("Jane.Smith")).thenReturn(Optional.of(trainee));

        traineeService.deleteTrainee("Jane.Smith", "password123");

        verify(authService, times(1)).validate("Jane.Smith", "password123");
        verify(traineeRepository, times(1)).delete(trainee);
    }
}