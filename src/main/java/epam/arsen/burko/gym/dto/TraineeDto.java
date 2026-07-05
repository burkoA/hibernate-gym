package epam.arsen.burko.gym.dto;

import java.time.LocalDate;

public record TraineeDto(
        Long id,
        String firstName,
        String lastName,
        String username,
        String password,
        Boolean isActive,
        LocalDate dateOfBirth,
        String address
) {
}

