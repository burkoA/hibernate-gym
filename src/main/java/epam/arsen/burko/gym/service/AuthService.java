package epam.arsen.burko.gym.service;

import epam.arsen.burko.gym.entity.User;
import epam.arsen.burko.gym.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;

    public void validate(String username, String password) {
        log.debug("Attempting to authenticate user: {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("Invalid password");
        }
        log.debug("User '{}' successfully authenticated", username);
    }
}
