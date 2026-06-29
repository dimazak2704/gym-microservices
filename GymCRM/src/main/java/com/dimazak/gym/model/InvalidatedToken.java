package com.dimazak.gym.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "invalidated_tokens")
public class InvalidatedToken {

    @Id
    @Column(name = "jti", nullable = false, updatable = false)
    private String jti;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    public InvalidatedToken() {}

    public InvalidatedToken(String jti, Instant expiresAt) {
        this.jti = jti;
        this.expiresAt = expiresAt;
    }

    public String getJti() { return jti; }
    public void setJti(String jti) { this.jti = jti; }

    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InvalidatedToken that = (InvalidatedToken) o;
        return Objects.equals(jti, that.jti);
    }

    @Override
    public int hashCode() { return Objects.hash(jti); }
}