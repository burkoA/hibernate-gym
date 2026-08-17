import epam.arsen.burko.gym.security.BruteForceProtectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BruteForceProtectionServiceTest {

    private BruteForceProtectionService bruteForceProtectionService;

    @BeforeEach
    void setUp() {
        bruteForceProtectionService = new BruteForceProtectionService();
    }

    @Test
    void recordFailedLogin_FirstAttempt_DoesNotLockUser() {
        bruteForceProtectionService.recordFailedLogin("user1");
        assertFalse(bruteForceProtectionService.isUserLocked("user1"));
        assertEquals(1, bruteForceProtectionService.getFailedAttempts("user1"));
    }

    @Test
    void recordFailedLogin_SecondAttempt_DoesNotLockUser() {
        bruteForceProtectionService.recordFailedLogin("user1");
        bruteForceProtectionService.recordFailedLogin("user1");
        assertFalse(bruteForceProtectionService.isUserLocked("user1"));
        assertEquals(2, bruteForceProtectionService.getFailedAttempts("user1"));
    }

    @Test
    void recordFailedLogin_ThirdAttempt_LocksUser() {
        bruteForceProtectionService.recordFailedLogin("user1");
        bruteForceProtectionService.recordFailedLogin("user1");
        bruteForceProtectionService.recordFailedLogin("user1");
        assertTrue(bruteForceProtectionService.isUserLocked("user1"));
        assertEquals(3, bruteForceProtectionService.getFailedAttempts("user1"));
    }

    @Test
    void recordSuccessfulLogin_ClearsFailedAttempts() {
        bruteForceProtectionService.recordFailedLogin("user1");
        bruteForceProtectionService.recordFailedLogin("user1");
        assertEquals(2, bruteForceProtectionService.getFailedAttempts("user1"));

        bruteForceProtectionService.recordSuccessfulLogin("user1");
        assertEquals(0, bruteForceProtectionService.getFailedAttempts("user1"));
    }

    @Test
    void isUserLocked_NonexistentUser_ReturnsFalse() {
        assertFalse(bruteForceProtectionService.isUserLocked("nonexistent"));
    }

    @Test
    void getFailedAttempts_NonexistentUser_ReturnsZero() {
        assertEquals(0, bruteForceProtectionService.getFailedAttempts("nonexistent"));
    }
}

