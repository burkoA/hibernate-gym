import epam.arsen.burko.gym.controller.AuthController;
import epam.arsen.burko.gym.dto.ChangeLoginRequest;
import epam.arsen.burko.gym.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @Test
    void login_ValidRequest_ReturnsOk() {
        ResponseEntity<Void> response = authController.login("john", "pwd");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(authService).validate("john", "pwd");
    }

    @Test
    void changeLogin_ValidRequest_ReturnsOk() {
        ChangeLoginRequest request = new ChangeLoginRequest("john", "old", "new");

        ResponseEntity<Void> response = authController.changeLogin(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(authService).changePassword("john", "old", "new");
    }
}

