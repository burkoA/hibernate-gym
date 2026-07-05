package epam.arsen.burko.gym.dto;

public record TrainerDto(
        Long id,
        String firstName,
        String lastName,
        String username,
        String password,
        Boolean isActive,
        Long specializationId,
        String specializationName
) {
}

