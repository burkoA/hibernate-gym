package epam.arsen.burko.gym.service;

import epam.arsen.burko.gym.entity.User;
import epam.arsen.burko.gym.exception.InvalidPasswordException;
import epam.arsen.burko.gym.exception.UserNotFoundException;
import epam.arsen.burko.gym.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;

    public void validate(String username, String password) {
        log.debug("Attempting to authenticate user: {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!user.getPassword().equals(password)) {
            throw new InvalidPasswordException("Invalid password");
        }
        log.debug("User '{}' successfully authenticated", username);
    }

    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword) {
        log.info("Processing password change for user: {}", username);
        validate(username, oldPassword);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        user.setPassword(newPassword);
        userRepository.save(user);
        log.info("Password successfully changed for user: {}", username);
    }
}
