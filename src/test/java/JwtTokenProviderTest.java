import epam.arsen.burko.gym.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", "MyVerySecureSecretKeyForJWTTokenGenerationAndValidation1234567890123456");
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationMs", 3600000L);
    }

    @Test
    void generateToken_ValidUsername_ReturnsToken() {
        String username = "John.Doe";
        String token = jwtTokenProvider.generateToken(username);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.contains("."));
    }

    @Test
    void getUsernameFromToken_ValidToken_ReturnsUsername() {
        String username = "John.Doe";
        String token = jwtTokenProvider.generateToken(username);

        String extractedUsername = jwtTokenProvider.getUsernameFromToken(token);
        assertEquals(username, extractedUsername);
    }

    @Test
    void validateToken_ValidToken_ReturnsTrue() {
        String username = "John.Doe";
        String token = jwtTokenProvider.generateToken(username);

        boolean isValid = jwtTokenProvider.validateToken(token);
        assertTrue(isValid);
    }

    @Test
    void validateToken_InvalidToken_ReturnsFalse() {
        String invalidToken = "invalid.token.here";

        boolean isValid = jwtTokenProvider.validateToken(invalidToken);
        assertFalse(isValid);
    }

    @Test
    void isTokenExpired_ValidToken_ReturnsFalse() {
        String username = "John.Doe";
        String token = jwtTokenProvider.generateToken(username);

        boolean isExpired = jwtTokenProvider.isTokenExpired(token);
        assertFalse(isExpired);
    }

    @Test
    void isTokenExpired_InvalidToken_ReturnsTrue() {
        String invalidToken = "invalid.token.here";

        boolean isExpired = jwtTokenProvider.isTokenExpired(invalidToken);
        assertTrue(isExpired);
    }

    @Test
    void generateToken_DifferentUsernames_GenerateDifferentTokens() {
        String token1 = jwtTokenProvider.generateToken("user1");
        String token2 = jwtTokenProvider.generateToken("user2");

        assertNotEquals(token1, token2);
        assertEquals("user1", jwtTokenProvider.getUsernameFromToken(token1));
        assertEquals("user2", jwtTokenProvider.getUsernameFromToken(token2));
    }
}


