package com.dimazak.workload.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private static final String SECRET =
            "a9f8e7d6c5b4a3f2e1d0c9b8a7f6e5d4c3b2a1f0e9d8c7b6a5f4e3d2c1b0a9f8";
    private static final String USERNAME = "Jane.Smith";

    private JwtService jwtService;
    private SecretKey key;

    @BeforeEach
    void setUp() {
        JwtProperties props = new JwtProperties();
        props.setSecret(SECRET);
        jwtService = new JwtService(props);
        key = Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    private String token(String subject) {
        return Jwts.builder()
                .subject(subject)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(key)
                .compact();
    }

    @Test
    void extractUsername_shouldReturnSubject() {
        assertEquals(USERNAME, jwtService.extractUsername(token(USERNAME)));
    }

    @Test
    void parseClaims_shouldThrowWhenTampered() {
        String t = token(USERNAME);
        String tampered = t.substring(0, t.length() - 4) + "AAAA";
        assertThrows(JwtException.class, () -> jwtService.extractUsername(tampered));
    }

    @Test
    void parseClaims_shouldThrowWhenDifferentKey() {
        SecretKey other = Keys.hmacShaKeyFor(
                "different-secret-key-with-at-least-32-bytes-length!".getBytes());
        String foreign = Jwts.builder().subject(USERNAME)
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(other).compact();

        assertThrows(JwtException.class, () -> jwtService.extractUsername(foreign));
    }
}