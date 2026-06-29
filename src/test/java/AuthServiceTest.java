import epam.arsen.burko.gym.entity.User;
import epam.arsen.burko.gym.repository.UserRepository;
import epam.arsen.burko.gym.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthService authService;

    @Test
    void validate_SuccessfulAuthentication_DoesNotThrow() {
        User user = new User();
        user.setUsername("John.Doe");
        user.setPassword("correctPassword");

        when(userRepository.findByUsername("John.Doe")).thenReturn(Optional.of(user));

        assertDoesNotThrow(() -> authService.validate("John.Doe", "correctPassword"));
        verify(userRepository, times(1)).findByUsername("John.Doe");
    }

    @Test
    void validate_UserNotFound_ThrowsException() {
        when(userRepository.findByUsername("Unknown.User")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authService.validate("Unknown.User", "anyPassword"));
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void validate_InvalidPassword_ThrowsException() {
        User user = new User();
        user.setUsername("John.Doe");
        user.setPassword("correctPassword");

        when(userRepository.findByUsername("John.Doe")).thenReturn(Optional.of(user));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authService.validate("John.Doe", "wrongPassword"));
        assertEquals("Invalid password", exception.getMessage());
    }
}