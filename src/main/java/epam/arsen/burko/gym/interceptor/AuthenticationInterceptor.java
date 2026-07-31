package epam.arsen.burko.gym.interceptor;

import epam.arsen.burko.gym.exception.AuthenticationRequiredException;
import epam.arsen.burko.gym.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AuthenticationInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationInterceptor.class);

    public static final String USERNAME_HEADER = "X-Username";
    public static final String PASSWORD_HEADER = "X-Password";

    private static final List<String> PUBLIC_PATHS = List.of(
            "/auth/login",
            "/trainees/register",
            "/trainers/register",
            "/environment/info",
            "/actuator/health",
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
        String method = request.getMethod();

        logger.debug("Processing request - Method: {}, Path: {}", method, path);

        if (PUBLIC_PATHS.stream().anyMatch(path::startsWith)) {
            logger.debug("Public path accessed: {}", path);
            return true;
        }

        String username = request.getHeader(USERNAME_HEADER);
        String password = request.getHeader(PASSWORD_HEADER);

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            logger.warn("Authentication failed for path {}: Missing credentials (username or password)", path);
            throw new AuthenticationRequiredException("Username and password are required");
        }

        try {
            logger.info("Authenticating user '{}' for path: {} {}", username, method, path);
            authService.validate(username, password);
            logger.debug("Authentication successful for user: {} on path: {}", username, path);
            return true;
        } catch (Exception e) {
            logger.warn("Authentication failed for user '{}' on path: {} - Error: {}", username, path, e.getMessage());
            throw e;
        }
    }
}




