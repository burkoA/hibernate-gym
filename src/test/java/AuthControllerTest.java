import epam.arsen.burko.gym.controller.AuthController;
import epam.arsen.burko.gym.dto.ChangeLoginRequest;
import epam.arsen.burko.gym.dto.LoginRequest;
import epam.arsen.burko.gym.dto.LoginResponse;
import epam.arsen.burko.gym.security.JwtTokenProvider;
import epam.arsen.burko.gym.security.TokenBlacklistService;
import epam.arsen.burko.gym.service.AuthService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @InjectMocks
    private AuthController authController;

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void login_ValidRequest_ReturnsOkWithToken() {
        LoginRequest request = new LoginRequest("john", "pwd");
        LoginResponse loginResponse = new LoginResponse("john", "jwt-token-123");
        when(authService.authenticate("john", "pwd")).thenReturn(loginResponse);

        ResponseEntity<LoginResponse> response = authController.login(request);

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

    @Test
    void logout_WithValidBearerToken_BlacklistsToken() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("john", null)
        );
        when(jwtTokenProvider.validateToken("jwt-token")).thenReturn(true);
        Date expiration = new Date(System.currentTimeMillis() + 60_000);
        when(jwtTokenProvider.getExpirationFromToken("jwt-token")).thenReturn(expiration);

        ResponseEntity<Void> response = authController.logout("Bearer jwt-token");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(tokenBlacklistService).blacklistToken("jwt-token", expiration);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void logout_WithoutBearerToken_DoesNotBlacklistToken() {
        ResponseEntity<Void> response = authController.logout(null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(tokenBlacklistService, never()).blacklistToken(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any(Date.class));
    }
}

