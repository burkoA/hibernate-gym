package epam.arsen.burko.gym.dto;

import java.time.LocalDate;
import java.util.List;

public record TraineeUpdateResponse(
        String username,
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        String address,
        Boolean isActive,
        List<TrainerSummaryDto> trainers
) {
}

