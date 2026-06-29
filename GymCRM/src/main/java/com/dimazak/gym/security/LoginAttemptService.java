package com.dimazak.gym.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TODO: Migrate to Redis for distributed state management.
 */
@Service
public class LoginAttemptService {

    private static final Logger log = LoggerFactory.getLogger(LoginAttemptService.class);
    private static final int MAX_ATTEMPTS = 3;
    private static final Duration BLOCK_DURATION = Duration.ofMinutes(5);

    private final Map<String, Attempt> attempts = new ConcurrentHashMap<>();

    public boolean isBlocked(String username) {
        Attempt attempt = attempts.get(username);
        if (attempt == null) {
            return false;
        }
        if (attempt.count < MAX_ATTEMPTS) {
            return false;
        }
        if (Instant.now().isAfter(attempt.blockedUntil)) {
            attempts.remove(username);
            log.debug("Block expired for user '{}', counter reset", username);
            return false;
        }
        return true;
    }

    public void recordFailure(String username) {
        Attempt updated = attempts.compute(username, (key, existing) -> {
            int count = (existing == null) ? 1 : existing.count + 1;
            Instant blockedUntil = Instant.now().plus(BLOCK_DURATION);
            return new Attempt(count, blockedUntil);
        });
        log.warn("Failed login attempt {}/{} for user '{}'",
                updated.count, MAX_ATTEMPTS, username);
    }

    public void recordSuccess(String username) {
        attempts.remove(username);
    }

    private static final class Attempt {
        private final int count;
        private final Instant blockedUntil;

        private Attempt(int count, Instant blockedUntil) {
            this.count = count;
            this.blockedUntil = blockedUntil;
        }
    }
}