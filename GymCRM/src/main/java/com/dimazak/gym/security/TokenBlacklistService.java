package com.dimazak.gym.security;

import com.dimazak.gym.dao.InvalidatedTokenDao;
import com.dimazak.gym.model.InvalidatedToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class TokenBlacklistService {

    private static final Logger log = LoggerFactory.getLogger(TokenBlacklistService.class);

    private final InvalidatedTokenDao invalidatedTokenDao;

    public TokenBlacklistService(InvalidatedTokenDao invalidatedTokenDao) {
        this.invalidatedTokenDao = invalidatedTokenDao;
    }

    @Transactional
    public void invalidate(String jti, Instant expiresAt) {
        if (invalidatedTokenDao.existsByJti(jti)) {
            log.debug("Token jti '{}' already invalidated", jti);
            return;
        }
        invalidatedTokenDao.save(new InvalidatedToken(jti, expiresAt));
        log.info("Token jti '{}' invalidated", jti);
    }

    @Transactional(readOnly = true)
    public boolean isInvalidated(String jti) {
        return invalidatedTokenDao.existsByJti(jti);
    }

    @Scheduled(fixedRateString = "${jwt.cleanup-rate-ms:3600000}")
    @Transactional
    public void purgeExpiredTokens() {
        int removed = invalidatedTokenDao.deleteExpired(Instant.now());
        if (removed > 0) {
            log.info("Purged {} expired tokens from blacklist", removed);
        }
    }
}