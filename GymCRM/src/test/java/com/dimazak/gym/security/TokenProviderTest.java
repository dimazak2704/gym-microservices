package com.dimazak.gym.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.*;

class TokenProviderTest {

    private final TokenProvider tokenProvider = new TokenProvider();

    @AfterEach
    void clear() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void getCurrentToken_shouldReturnHeaderValue() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer xyz");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        assertEquals("Bearer xyz", tokenProvider.getCurrentToken());
    }

    @Test
    void getCurrentToken_shouldReturnNullWhenNoRequestContext() {
        RequestContextHolder.resetRequestAttributes();
        assertNull(tokenProvider.getCurrentToken());
    }

    @Test
    void getCurrentToken_shouldReturnNullWhenNoHeader() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        assertNull(tokenProvider.getCurrentToken());
    }
}