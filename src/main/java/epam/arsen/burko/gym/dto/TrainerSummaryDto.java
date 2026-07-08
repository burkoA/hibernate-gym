package epam.arsen.burko.gym.dto;

public record TrainerSummaryDto(
        String username,
        String firstName,
        String lastName,
        Long specializationId,
        String specializationName
) {
}

