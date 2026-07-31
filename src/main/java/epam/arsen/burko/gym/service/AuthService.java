package epam.arsen.burko.gym.service;

import epam.arsen.burko.gym.dto.LoginResponse;
import epam.arsen.burko.gym.entity.User;
import epam.arsen.burko.gym.exception.InvalidPasswordException;
import epam.arsen.burko.gym.exception.UserLockedException;
import epam.arsen.burko.gym.exception.UserNotFoundException;
import epam.arsen.burko.gym.repository.UserRepository;
import epam.arsen.burko.gym.security.BruteForceProtectionService;
import epam.arsen.burko.gym.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final BruteForceProtectionService bruteForceProtectionService;
    private final JwtTokenProvider jwtTokenProvider;

    public LoginResponse authenticate(String username, String password) {
        log.debug("Attempting to authenticate user: {}", username);

        if (bruteForceProtectionService.isUserLocked(username)) {
            log.warn("Login attempt for locked user: {}", username);
            throw new UserLockedException("User account is temporarily locked due to too many failed login attempts. Please try again in 5 minutes.");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            bruteForceProtectionService.recordFailedLogin(username);
            throw new InvalidPasswordException("Invalid password");
        }

        bruteForceProtectionService.recordSuccessfulLogin(username);
        String token = jwtTokenProvider.generateToken(username);
        log.debug("User '{}' successfully authenticated and JWT token generated", username);
        return new LoginResponse(username, token);
    }

    public void validate(String username, String password) {
        log.debug("Attempting to authenticate user: {}", username);
        
        if (bruteForceProtectionService.isUserLocked(username)) {
            log.warn("Login attempt for locked user: {}", username);
            throw new UserLockedException("User account is temporarily locked due to too many failed login attempts. Please try again in 5 minutes.");
        }
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            bruteForceProtectionService.recordFailedLogin(username);
            throw new InvalidPasswordException("Invalid password");
        }
        
        bruteForceProtectionService.recordSuccessfulLogin(username);
        log.debug("User '{}' successfully authenticated", username);
    }

    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword) {
        log.info("Processing password change for user: {}", username);
        validate(username, oldPassword);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Password successfully changed for user: {}", username);
    }
}
