import epam.arsen.burko.gym.controller.AuthController;
import epam.arsen.burko.gym.dto.ChangeLoginRequest;
import epam.arsen.burko.gym.dto.LoginResponse;
import epam.arsen.burko.gym.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @Test
    void login_ValidRequest_ReturnsOkWithToken() {
        LoginResponse loginResponse = new LoginResponse("john", "jwt-token-123");
        when(authService.authenticate("john", "pwd")).thenReturn(loginResponse);

        ResponseEntity<LoginResponse> response = authController.login("john", "pwd");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("john", response.getBody().username());
        assertEquals("jwt-token-123", response.getBody().token());
        verify(authService).authenticate("john", "pwd");
    }

    @Test
    void changeLogin_ValidRequest_ReturnsOk() {
        ChangeLoginRequest request = new ChangeLoginRequest("john", "old", "new");

        ResponseEntity<Void> response = authController.changeLogin(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(authService).changePassword("john", "old", "new");
    }
}

