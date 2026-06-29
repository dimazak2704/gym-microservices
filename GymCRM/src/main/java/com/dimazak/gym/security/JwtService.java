package com.dimazak.gym.security;

import com.dimazak.gym.model.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);
    private static final String CLAIM_ROLE = "role";

    private final SecretKey signingKey;
    private final long expirationMs;

    public JwtService(JwtProperties properties) {
        this.signingKey = Keys.hmacShaKeyFor(properties.getSecret().getBytes());
        this.expirationMs = properties.getExpirationMs();
    }

    public String generateToken(String username, Role role) {
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(expirationMs);
        String jti = UUID.randomUUID().toString();

        log.debug("Generating token for user '{}' with jti '{}'", username, jti);

        return Jwts.builder()
                .id(jti)
                .subject(username)
                .claim(CLAIM_ROLE, role.name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(signingKey)
                .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public String extractJti(String token) {
        return parseClaims(token).getId();
    }

    public Role extractRole(String token) {
        return Role.valueOf(parseClaims(token).get(CLAIM_ROLE, String.class));
    }

    public Instant extractExpiration(String token) {
        return parseClaims(token).getExpiration().toInstant();
    }
}