package epam.arsen.burko.gym.dto;

public record LoginResponse(
        String username,
        String token,
        String tokenType
) {
    public LoginResponse(String username, String token) {
        this(username, token, "Bearer");
    }
}

