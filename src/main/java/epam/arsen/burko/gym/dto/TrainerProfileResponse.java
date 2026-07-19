package epam.arsen.burko.gym.dto;

import java.util.List;

public record TrainerProfileResponse(
        String firstName,
        String lastName,
        Long specializationId,
        String specializationName,
        Boolean isActive,
        List<TraineeSummaryDto> trainees
) {
}

