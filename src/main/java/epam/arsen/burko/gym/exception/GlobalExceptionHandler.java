package epam.arsen.burko.gym.exception;

import jakarta.validation.ConstraintViolationException;
import org.slf4j.MDC;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private void addTransactionId(ProblemDetail problem) {
        problem.setProperty("transactionId", MDC.get("transactionId"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationException(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .toList();
        log.warn("Validation failed: {}", errors);
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Validation Failed");
        problem.setProperty("errors", errors);
        addTransactionId(problem);
        return problem;
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MissingServletRequestParameterException.class, MethodArgumentTypeMismatchException.class})
    public ProblemDetail handleBadRequest(Exception ex) {
        log.warn("Bad request: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Bad Request");
        problem.setDetail(ex.getMessage());
        addTransactionId(problem);
        return problem;
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ProblemDetail handleMissingHeader(MissingRequestHeaderException ex) {
        log.warn("Missing authentication header: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Validation Failed");
        problem.setDetail(ex.getMessage());
        addTransactionId(problem);
        return problem;
    }

    @ExceptionHandler(TraineeNotFoundException.class)
    public ProblemDetail handleTraineeNotFound(TraineeNotFoundException ex) {
        log.warn("Trainee not found: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problem.setTitle("Trainee Not Found");
        problem.setDetail(ex.getMessage());
        addTransactionId(problem);
        return problem;
    }

    @ExceptionHandler(TrainerNotFoundException.class)
    public ProblemDetail handleTrainerNotFound(TrainerNotFoundException ex) {
        log.warn("Trainer not found: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problem.setTitle("Trainer Not Found");
        problem.setDetail(ex.getMessage());
        addTransactionId(problem);
        return problem;
    }

    @ExceptionHandler(SpecializationNotFoundException.class)
    public ProblemDetail handleSpecializationNotFound(SpecializationNotFoundException ex) {
        log.warn("Specialization not found: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problem.setTitle("Specialization Not Found");
        problem.setDetail(ex.getMessage());
        addTransactionId(problem);
        return problem;
    }

    @ExceptionHandler(RoleConflictException.class)
    public ProblemDetail handleRoleConflict(RoleConflictException ex) {
        log.warn("Role conflict: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problem.setTitle("Conflict");
        problem.setDetail(ex.getMessage());
        addTransactionId(problem);
        return problem;
    }

    @ExceptionHandler(AuthenticationRequiredException.class)
    public ProblemDetail handleAuthenticationRequired(AuthenticationRequiredException ex) {
        log.warn("Authentication required: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Validation Failed");
        problem.setDetail(ex.getMessage());
        addTransactionId(problem);
        return problem;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolation(ConstraintViolationException ex) {
        List<String> errors = ex.getConstraintViolations().stream()
                .map(cv -> cv.getPropertyPath() + ": " + cv.getMessage())
                .toList();
        log.warn("Constraint violation: {}", errors);
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Validation Failed");
        problem.setProperty("errors", errors);
        addTransactionId(problem);
        return problem;
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ProblemDetail handleUserNotFound(UserNotFoundException ex) {
        log.warn("User not found: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problem.setTitle("User Not Found");
        problem.setDetail(ex.getMessage());
        addTransactionId(problem);
        return problem;
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ProblemDetail handleInvalidPassword(InvalidPasswordException ex) {
        log.warn("Invalid credentials: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
        problem.setTitle("Unauthorized");
        problem.setDetail(ex.getMessage());
        addTransactionId(problem);
        return problem;
    }

    @ExceptionHandler(UserLockedException.class)
    public ProblemDetail handleUserLocked(UserLockedException ex) {
        log.warn("User account locked: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.LOCKED);
        problem.setTitle("Account Locked");
        problem.setDetail(ex.getMessage());
        addTransactionId(problem);
        return problem;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneral(Exception ex) {
        log.error("Unexpected error", ex);
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        problem.setTitle("Internal Server Error");
        problem.setDetail("An unexpected error occurred");
        addTransactionId(problem);
        return problem;
    }
}

