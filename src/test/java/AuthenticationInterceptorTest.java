import epam.arsen.burko.gym.exception.AuthenticationRequiredException;
import epam.arsen.burko.gym.interceptor.AuthenticationInterceptor;
import epam.arsen.burko.gym.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthenticationInterceptorTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthenticationInterceptor interceptor;

    @Test
    void preHandle_PublicPath_AllowsWithoutAuthentication() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath("/api");
        request.setRequestURI("/api/trainees/register");
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertDoesNotThrow(() -> interceptor.preHandle(request, response, new Object()));
        verify(authService, never()).validate(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void preHandle_ProtectedPath_WithValidHeaders_AllowsRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath("/api");
        request.setRequestURI("/api/trainees/Jane.Smith");
        request.addHeader(AuthenticationInterceptor.USERNAME_HEADER, "Jane.Smith");
        request.addHeader(AuthenticationInterceptor.PASSWORD_HEADER, "password123");
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertDoesNotThrow(() -> interceptor.preHandle(request, response, new Object()));
        verify(authService).validate("Jane.Smith", "password123");
    }

    @Test
    void preHandle_ProtectedPath_MissingHeaders_ThrowsException() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath("/api");
        request.setRequestURI("/api/trainees/Jane.Smith");
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertThrows(AuthenticationRequiredException.class, () -> interceptor.preHandle(request, response, new Object()));
        verify(authService, never()).validate(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString());
    }
}


