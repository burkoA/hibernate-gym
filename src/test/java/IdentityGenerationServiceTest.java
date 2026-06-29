import epam.arsen.burko.gym.entity.User;
import epam.arsen.burko.gym.repository.UserRepository;
import epam.arsen.burko.gym.service.IdentityGenerationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IdentityGenerationServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private IdentityGenerationService identityService;

    @Test
    void generateUsername_NoExistingUsers_ReturnsBaseUsername() {
        when(userRepository.findByUsernameStartingWith("John.Doe")).thenReturn(List.of());

        String result = identityService.generateUsername("John", "Doe");

        assertEquals("John.Doe", result);
    }

    @Test
    void generateUsername_ExistingUsers_ReturnsIncrementedUsername() {
        User user1 = new User(); user1.setUsername("John.Doe");
        User user2 = new User(); user2.setUsername("John.Doe1");
        User user3 = new User(); user3.setUsername("John.Doe2");

        when(userRepository.findByUsernameStartingWith("John.Doe"))
                .thenReturn(List.of(user1, user2, user3));

        String result = identityService.generateUsername("John", "Doe");

        assertEquals("John.Doe3", result);
    }

    @Test
    void generatePassword_ReturnsTenCharacterRandomString() {
        String password = identityService.generatePassword();

        assertNotNull(password);
        assertEquals(10, password.length());
    }
}