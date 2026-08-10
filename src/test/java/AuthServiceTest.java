import epam.arsen.burko.gym.dto.LoginResponse;
import epam.arsen.burko.gym.entity.User;
import epam.arsen.burko.gym.exception.InvalidPasswordException;
import epam.arsen.burko.gym.exception.UserLockedException;
import epam.arsen.burko.gym.exception.UserNotFoundException;
import epam.arsen.burko.gym.repository.UserRepository;
import epam.arsen.burko.gym.security.BruteForceProtectionService;
import epam.arsen.burko.gym.security.JwtTokenProvider;
import epam.arsen.burko.gym.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private BruteForceProtectionService bruteForceProtectionService;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    @Test
    void validate_SuccessfulAuthentication_DoesNotThrow() {
        User user = new User();
        user.setUsername("John.Doe");
        user.setPassword("hashedPassword");

        when(bruteForceProtectionService.isUserLocked("John.Doe")).thenReturn(false);
        when(userRepository.findByUsername("John.Doe")).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(mock(org.springframework.security.core.Authentication.class));

        assertDoesNotThrow(() -> authService.validate("John.Doe", "correctPassword"));
        verify(userRepository, times(1)).findByUsername("John.Doe");
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(bruteForceProtectionService, times(1)).recordSuccessfulLogin("John.Doe");
    }

    @Test
    void validate_UserLocked_ThrowsException() {
        when(bruteForceProtectionService.isUserLocked("John.Doe")).thenReturn(true);

        UserLockedException exception = assertThrows(UserLockedException.class,
                () -> authService.validate("John.Doe", "anyPassword"));
        assertTrue(exception.getMessage().contains("temporarily locked"));
    }

    @Test
    void validate_UserNotFound_ThrowsException() {
        when(bruteForceProtectionService.isUserLocked("Unknown.User")).thenReturn(false);
        when(userRepository.findByUsername("Unknown.User")).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> authService.validate("Unknown.User", "anyPassword"));
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void validate_InvalidPassword_ThrowsException() {
        User user = new User();
        user.setUsername("John.Doe");
        user.setPassword("hashedPassword");

        when(bruteForceProtectionService.isUserLocked("John.Doe")).thenReturn(false);
        when(userRepository.findByUsername("John.Doe")).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        InvalidPasswordException exception = assertThrows(InvalidPasswordException.class,
                () -> authService.validate("John.Doe", "wrongPassword"));
        assertEquals("Invalid password", exception.getMessage());
        verify(bruteForceProtectionService, times(1)).recordFailedLogin("John.Doe");
    }

    @Test
    void authenticate_SuccessfulAuthentication_ReturnsLoginResponse() {
        User user = new User();
        user.setUsername("John.Doe");
        user.setPassword("hashedPassword");

        when(bruteForceProtectionService.isUserLocked("John.Doe")).thenReturn(false);
        when(userRepository.findByUsername("John.Doe")).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(mock(org.springframework.security.core.Authentication.class));
        when(jwtTokenProvider.generateToken("John.Doe")).thenReturn("jwt-token-123");

        LoginResponse response = authService.authenticate("John.Doe", "correctPassword");

        assertNotNull(response);
        assertEquals("John.Doe", response.username());
        assertEquals("jwt-token-123", response.token());
        assertEquals("Bearer", response.tokenType());
        verify(bruteForceProtectionService, times(1)).recordSuccessfulLogin("John.Doe");
        verify(jwtTokenProvider, times(1)).generateToken("John.Doe");
    }

    @Test
    void authenticate_InvalidPassword_ThrowsException() {
        User user = new User();
        user.setUsername("John.Doe");
        user.setPassword("hashedPassword");

        when(bruteForceProtectionService.isUserLocked("John.Doe")).thenReturn(false);
        when(userRepository.findByUsername("John.Doe")).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        InvalidPasswordException exception = assertThrows(InvalidPasswordException.class,
                () -> authService.authenticate("John.Doe", "wrongPassword"));
        assertEquals("Invalid password", exception.getMessage());
        verify(bruteForceProtectionService, times(1)).recordFailedLogin("John.Doe");
        verify(jwtTokenProvider, never()).generateToken(anyString());
    }

    @Test
    void changePassword_ValidOldPassword_UpdatesAndSavesUser() {
        User user = new User();
        user.setUsername("John.Doe");
        user.setPassword("hashedOldPassword");

        when(bruteForceProtectionService.isUserLocked("John.Doe")).thenReturn(false);
        when(userRepository.findByUsername("John.Doe")).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(mock(org.springframework.security.core.Authentication.class));
        when(passwordEncoder.encode("newPass")).thenReturn("hashedNewPassword");

        authService.changePassword("John.Doe", "oldPass", "newPass");

        assertEquals("hashedNewPassword", user.getPassword());
        verify(userRepository).save(user);
    }

    @Test
    void changePassword_InvalidOldPassword_ThrowsAndDoesNotSave() {
        User user = new User();
        user.setUsername("John.Doe");
        user.setPassword("hashedOldPassword");

        when(bruteForceProtectionService.isUserLocked("John.Doe")).thenReturn(false);
        when(userRepository.findByUsername("John.Doe")).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        InvalidPasswordException exception = assertThrows(
                InvalidPasswordException.class,
                () -> authService.changePassword("John.Doe", "wrongPass", "newPass")
        );

        assertEquals("Invalid password", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }
}