package epam.arsen.burko.gym.dto;

public record TrainerUpdateDto(
        String firstName,
        String lastName,
        Long specializationId
) {
}

