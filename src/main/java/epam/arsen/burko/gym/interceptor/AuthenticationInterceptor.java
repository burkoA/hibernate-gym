package epam.arsen.burko.gym.interceptor;

import epam.arsen.burko.gym.exception.AuthenticationRequiredException;
import epam.arsen.burko.gym.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AuthenticationInterceptor implements HandlerInterceptor {

    public static final String USERNAME_HEADER = "X-Username";
    public static final String PASSWORD_HEADER = "X-Password";

    private static final List<String> PUBLIC_PATHS = List.of(
            "/auth/login",
            "/trainees/register",
            "/trainers/register",
            "/v2/api-docs",
            "/swagger-resources",
            "/swagger-ui.html",
            "/swagger-ui",
            "/webjars",
            "/error"
    );

    private final AuthService authService;

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        if (PUBLIC_PATHS.stream().anyMatch(path::startsWith)) {
            return true;
        }

        String username = request.getHeader(USERNAME_HEADER);
        String password = request.getHeader(PASSWORD_HEADER);
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            throw new AuthenticationRequiredException("Username and password are required");
        }

        authService.validate(username, password);
        return true;
    }
}




