package epam.arsen.burko.gym.dto;

import java.time.LocalDate;

public record TraineeUpdateDto(
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        String address
) {
}

