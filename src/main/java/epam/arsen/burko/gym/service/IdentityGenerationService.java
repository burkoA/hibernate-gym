package epam.arsen.burko.gym.service;

import epam.arsen.burko.gym.entity.User;
import epam.arsen.burko.gym.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IdentityGenerationService {
    private final UserRepository userRepository;

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private final SecureRandom random = new SecureRandom();

    public String generateUsername(String firstName, String lastName) {
        String baseUsername = firstName + "." + lastName;
        List<User> existingUsers = userRepository.findByUsernameStartingWith(baseUsername);

        if (existingUsers.isEmpty()) return baseUsername;

        int maxSerial = existingUsers.stream()
                .map(User::getUsername)
                .filter(u -> u.length() > baseUsername.length())
                .mapToInt(u -> {
                    try { return Integer.parseInt(u.substring(baseUsername.length())); }
                    catch (NumberFormatException e) { return 0; }
                })
                .max().orElse(0);

        return baseUsername + (maxSerial + 1);
    }

    public String generatePassword() {
        StringBuilder password = new StringBuilder(10);
        for (int i = 0; i < 10; i++) {
            password.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return password.toString();
    }
}
