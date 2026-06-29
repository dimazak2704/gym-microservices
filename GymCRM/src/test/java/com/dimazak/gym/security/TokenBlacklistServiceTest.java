package com.dimazak.gym.security;

import com.dimazak.gym.dao.InvalidatedTokenDao;
import com.dimazak.gym.model.InvalidatedToken;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenBlacklistServiceTest {

    private static final String JTI = "jti-uuid";

    @Mock private InvalidatedTokenDao invalidatedTokenDao;

    @InjectMocks
    private TokenBlacklistService service;

    @Test
    void invalidate_shouldPersistTokenWhenNotPresent() {
        when(invalidatedTokenDao.existsByJti(JTI)).thenReturn(false);
        Instant exp = Instant.now().plusSeconds(60);

        service.invalidate(JTI, exp);

        ArgumentCaptor<InvalidatedToken> captor = ArgumentCaptor.forClass(InvalidatedToken.class);
        verify(invalidatedTokenDao).save(captor.capture());
        assertEquals(JTI, captor.getValue().getJti());
        assertEquals(exp, captor.getValue().getExpiresAt());
    }

    @Test
    void invalidate_shouldNoOpWhenAlreadyInvalidated() {
        when(invalidatedTokenDao.existsByJti(JTI)).thenReturn(true);

        service.invalidate(JTI, Instant.now());

        verify(invalidatedTokenDao, never()).save(any());
    }

    @Test
    void isInvalidated_shouldReturnDaoResult() {
        when(invalidatedTokenDao.existsByJti(JTI)).thenReturn(true);
        assertTrue(service.isInvalidated(JTI));
    }

    @Test
    void purgeExpiredTokens_shouldDelegateToDao() {
        when(invalidatedTokenDao.deleteExpired(any(Instant.class))).thenReturn(5);

        service.purgeExpiredTokens();

        verify(invalidatedTokenDao).deleteExpired(any(Instant.class));
    }
}