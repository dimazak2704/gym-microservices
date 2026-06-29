package com.dimazak.gym.security;

import com.dimazak.gym.model.Role;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.WeakKeyException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private static final String SECRET = "test-secret-key-that-is-at-least-32-bytes-long-ok!";
    private static final long EXPIRATION_MS = 3600_000L;
    private static final String USERNAME = "John.Doe";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret(SECRET);
        properties.setExpirationMs(EXPIRATION_MS);
        jwtService = new JwtService(properties);
    }

    @Test
    void generateToken_shouldProduceValidToken() {
        String token = jwtService.generateToken(USERNAME, Role.TRAINEE);

        assertNotNull(token);
        assertEquals(3, token.split("\\.").length, "JWT must have 3 parts");
    }

    @Test
    void extractUsername_shouldReturnSubject() {
        String token = jwtService.generateToken(USERNAME, Role.TRAINEE);
        assertEquals(USERNAME, jwtService.extractUsername(token));
    }

    @Test
    void extractRole_shouldReturnRoleClaim() {
        String token = jwtService.generateToken(USERNAME, Role.TRAINER);
        assertEquals(Role.TRAINER, jwtService.extractRole(token));
    }

    @Test
    void extractJti_shouldReturnUniqueIdPerToken() {
        String token1 = jwtService.generateToken(USERNAME, Role.TRAINEE);
        String token2 = jwtService.generateToken(USERNAME, Role.TRAINEE);

        String jti1 = jwtService.extractJti(token1);
        String jti2 = jwtService.extractJti(token2);

        assertNotNull(jti1);
        assertNotNull(jti2);
        assertNotEquals(jti1, jti2);
    }

    @Test
    void extractExpiration_shouldBeFutureWithinExpiration() {
        Instant before = Instant.now();
        String token = jwtService.generateToken(USERNAME, Role.TRAINEE);
        Instant exp = jwtService.extractExpiration(token);

        assertTrue(exp.isAfter(before));
        assertTrue(exp.isBefore(before.plusMillis(EXPIRATION_MS + 5_000)));
    }

    @Test
    void parseClaims_shouldThrowWhenTokenTamperedWith() {
        String token = jwtService.generateToken(USERNAME, Role.TRAINEE);
        String tampered = token.substring(0, token.length() - 4) + "AAAA";

        assertThrows(JwtException.class, () -> jwtService.extractUsername(tampered));
    }

    @Test
    void parseClaims_shouldThrowWhenSignedWithDifferentKey() {
        JwtProperties otherProps = new JwtProperties();
        otherProps.setSecret("different-secret-key-with-32+-bytes-length-here-ok!");
        otherProps.setExpirationMs(EXPIRATION_MS);
        JwtService otherService = new JwtService(otherProps);

        String foreignToken = otherService.generateToken(USERNAME, Role.TRAINEE);

        assertThrows(JwtException.class, () -> jwtService.extractUsername(foreignToken));
    }

    @Test
    void constructor_shouldThrowWhenSecretTooShort() {
        JwtProperties bad = new JwtProperties();
        bad.setSecret("short");
        bad.setExpirationMs(EXPIRATION_MS);

        assertThrows(WeakKeyException.class, () -> new JwtService(bad));
    }
}