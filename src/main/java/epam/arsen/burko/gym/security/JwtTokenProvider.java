package epam.arsen.burko.gym.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class JwtTokenProvider {

    @Value("${jwt.secret:MyVerySecureSecretKeyForJWTTokenGenerationAndValidation1234567890123456}")
    private String jwtSecret;

    @Value("${jwt.expiration:3600000}")
    private long jwtExpirationMs;

    public String generateToken(String username) {
        log.debug("Generating JWT token for user: {}", username);
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, username);
    }

    private String createToken(Map<String, Object> claims, String subject) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        String token = Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();

        log.debug("JWT token generated successfully for user: {}", subject);
        return token;
    }

    public String getUsernameFromToken(String token) {
        log.debug("Extracting username from JWT token");
        return getClaims(token).getSubject();
    }

    public Date getExpirationFromToken(String token) {
        log.debug("Extracting expiration from JWT token");
        return getClaims(token).getExpiration();
    }

    public boolean validateToken(String token) {
        try {
            log.debug("Validating JWT token");
            getClaims(token);
            log.debug("JWT token is valid");
            return true;
        } catch (Exception ex) {
            log.warn("Invalid JWT token: {}", ex.getMessage());
            return false;
        }
    }

    private Claims getClaims(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isTokenExpired(String token) {
        try {
            Claims claims = getClaims(token);
            return claims.getExpiration().before(new Date());
        } catch (Exception ex) {
            log.warn("Error checking token expiration: {}", ex.getMessage());
            return true;
        }
    }
}



