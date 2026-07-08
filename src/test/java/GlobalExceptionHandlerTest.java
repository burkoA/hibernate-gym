import epam.arsen.burko.gym.exception.AuthenticationRequiredException;
import epam.arsen.burko.gym.exception.GlobalExceptionHandler;
import epam.arsen.burko.gym.exception.InvalidPasswordException;
import epam.arsen.burko.gym.exception.RoleConflictException;
import epam.arsen.burko.gym.exception.SpecializationNotFoundException;
import epam.arsen.burko.gym.exception.TraineeNotFoundException;
import epam.arsen.burko.gym.exception.TrainerNotFoundException;
import epam.arsen.burko.gym.exception.UserNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.core.MethodParameter;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;

import java.lang.reflect.Method;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @BeforeEach
    void setup() {
        MDC.put("transactionId", "tx-1");
    }

    @AfterEach
    void cleanup() {
        MDC.clear();
    }

    @Test
    void handleValidationException_ReturnsBadRequestWithErrorsAndTransactionId() throws Exception {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(new FieldError("request", "username", "is required"));

        Method method = Dummy.class.getDeclaredMethod("sample", String.class);
        MethodParameter parameter = new MethodParameter(method, 0);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, bindingResult);

        ProblemDetail problem = handler.handleValidationException(ex);
        var properties = problem.getProperties();

        assertEquals(HttpStatus.BAD_REQUEST.value(), problem.getStatus());
        assertEquals("Validation Failed", problem.getTitle());
        assertNotNull(properties);
        assertTrue(properties.containsKey("errors"));
        assertEquals("tx-1", properties.get("transactionId"));
    }

    @Test
    void handleBadRequest_ReturnsBadRequestProblem() {
        ProblemDetail problem = handler.handleBadRequest(
                new HttpMessageNotReadableException("Malformed JSON", new MockHttpInputMessage(new byte[0]))
        );
        var properties = problem.getProperties();

        assertEquals(HttpStatus.BAD_REQUEST.value(), problem.getStatus());
        assertEquals("Bad Request", problem.getTitle());
        assertNotNull(properties);
        assertEquals("tx-1", properties.get("transactionId"));
    }

    @Test
    void handleMissingHeader_ReturnsBadRequestProblem() throws Exception {
        Method method = Dummy.class.getDeclaredMethod("sample", String.class);
        MethodParameter parameter = new MethodParameter(method, 0);
        MissingRequestHeaderException ex = new MissingRequestHeaderException("X-Auth", parameter);

        ProblemDetail problem = handler.handleMissingHeader(ex);
        var properties = problem.getProperties();

        assertEquals(HttpStatus.BAD_REQUEST.value(), problem.getStatus());
        assertEquals("Validation Failed", problem.getTitle());
        assertNotNull(properties);
        assertEquals("tx-1", properties.get("transactionId"));
    }

    @Test
    void handleTraineeNotFound_ReturnsNotFoundProblem() {
        ProblemDetail problem = handler.handleTraineeNotFound(new TraineeNotFoundException("Trainee missing"));

        assertEquals(HttpStatus.NOT_FOUND.value(), problem.getStatus());
        assertEquals("Trainee Not Found", problem.getTitle());
        assertEquals("Trainee missing", problem.getDetail());
    }

    @Test
    void handleTrainerNotFound_ReturnsNotFoundProblem() {
        ProblemDetail problem = handler.handleTrainerNotFound(new TrainerNotFoundException("Trainer missing"));

        assertEquals(HttpStatus.NOT_FOUND.value(), problem.getStatus());
        assertEquals("Trainer Not Found", problem.getTitle());
    }

    @Test
    void handleSpecializationNotFound_ReturnsNotFoundProblem() {
        ProblemDetail problem = handler.handleSpecializationNotFound(new SpecializationNotFoundException("Specialization missing"));

        assertEquals(HttpStatus.NOT_FOUND.value(), problem.getStatus());
        assertEquals("Specialization Not Found", problem.getTitle());
    }

    @Test
    void handleRoleConflict_ReturnsConflictProblem() {
        ProblemDetail problem = handler.handleRoleConflict(new RoleConflictException("Conflict"));

        assertEquals(HttpStatus.CONFLICT.value(), problem.getStatus());
        assertEquals("Conflict", problem.getTitle());
    }

    @Test
    void handleAuthenticationRequired_ReturnsBadRequestProblem() {
        ProblemDetail problem = handler.handleAuthenticationRequired(new AuthenticationRequiredException("Auth required"));

        assertEquals(HttpStatus.BAD_REQUEST.value(), problem.getStatus());
        assertEquals("Validation Failed", problem.getTitle());
    }

    @Test
    void handleConstraintViolation_ReturnsBadRequestWithErrors() {
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);
        when(path.toString()).thenReturn("username");
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn("must not be blank");

        ConstraintViolationException ex = new ConstraintViolationException(Set.of(violation));

        ProblemDetail problem = handler.handleConstraintViolation(ex);
        var properties = problem.getProperties();

        assertEquals(HttpStatus.BAD_REQUEST.value(), problem.getStatus());
        assertEquals("Validation Failed", problem.getTitle());
        assertNotNull(properties);
        assertTrue(properties.containsKey("errors"));
    }

    @Test
    void handleUserNotFound_ReturnsNotFoundProblem() {
        ProblemDetail problem = handler.handleUserNotFound(new UserNotFoundException("User missing"));

        assertEquals(HttpStatus.NOT_FOUND.value(), problem.getStatus());
        assertEquals("User Not Found", problem.getTitle());
    }

    @Test
    void handleInvalidPassword_ReturnsUnauthorizedProblem() {
        ProblemDetail problem = handler.handleInvalidPassword(new InvalidPasswordException("Invalid"));

        assertEquals(HttpStatus.UNAUTHORIZED.value(), problem.getStatus());
        assertEquals("Unauthorized", problem.getTitle());
    }

    @Test
    void handleGeneral_ReturnsInternalServerErrorProblem() {
        ProblemDetail problem = handler.handleGeneral(new RuntimeException("boom"));
        var properties = problem.getProperties();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), problem.getStatus());
        assertEquals("Internal Server Error", problem.getTitle());
        assertEquals("An unexpected error occurred", problem.getDetail());
        assertNotNull(properties);
        assertEquals("tx-1", properties.get("transactionId"));
    }

    private static class Dummy {
        @SuppressWarnings("unused")
        void sample(String value) {
            // Intentionally empty: reflection target for MethodParameter creation in tests.
        }
    }
}


