package epam.arsen.burko.gym.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class BruteForceProtectionService {

    private static final int MAX_ATTEMPTS = 3;
    private static final long LOCK_DURATION_MINUTES = 5;

    private final Map<String, LoginAttempt> loginAttempts = new HashMap<>();

    public void recordFailedLogin(String username) {
        log.warn("Recording failed login attempt for user: {}", username);
        LoginAttempt attempt = loginAttempts.computeIfAbsent(username, k -> new LoginAttempt());
        attempt.incrementFailedAttempts();
        attempt.setLastAttemptTime(LocalDateTime.now());

        if (attempt.getFailedAttempts() >= MAX_ATTEMPTS) {
            attempt.setLockedUntil(LocalDateTime.now().plusMinutes(LOCK_DURATION_MINUTES));
            log.warn("User '{}' has been locked for {} minutes due to {} failed login attempts",
                    username, LOCK_DURATION_MINUTES, MAX_ATTEMPTS);
        }
    }

    public void recordSuccessfulLogin(String username) {
        log.debug("Clearing failed login attempts for user: {}", username);
        loginAttempts.remove(username);
    }

    public boolean isUserLocked(String username) {
        LoginAttempt attempt = loginAttempts.get(username);
        if (attempt == null) {
            return false;
        }

        if (attempt.getLockedUntil() != null && LocalDateTime.now().isBefore(attempt.getLockedUntil())) {
            log.warn("User '{}' is currently locked until: {}", username, attempt.getLockedUntil());
            return true;
        }

        if (attempt.getLockedUntil() != null && LocalDateTime.now().isAfter(attempt.getLockedUntil())) {
            loginAttempts.remove(username);
            log.info("User '{}' has been automatically unlocked", username);
            return false;
        }

        return false;
    }

    public int getFailedAttempts(String username) {
        LoginAttempt attempt = loginAttempts.get(username);
        return attempt != null ? attempt.getFailedAttempts() : 0;
    }

    public static class LoginAttempt {
        private int failedAttempts = 0;
        private LocalDateTime lastAttemptTime;
        private LocalDateTime lockedUntil;

        public int getFailedAttempts() {
            return failedAttempts;
        }

        public void incrementFailedAttempts() {
            this.failedAttempts++;
        }

        public LocalDateTime getLastAttemptTime() {
            return lastAttemptTime;
        }

        public void setLastAttemptTime(LocalDateTime lastAttemptTime) {
            this.lastAttemptTime = lastAttemptTime;
        }

        public LocalDateTime getLockedUntil() {
            return lockedUntil;
        }

        public void setLockedUntil(LocalDateTime lockedUntil) {
            this.lockedUntil = lockedUntil;
        }
    }
}

