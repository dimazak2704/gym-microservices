package com.dimazak.gym.security;

import com.dimazak.gym.model.Role;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    private static final String TOKEN = "header.payload.sig";
    private static final String USERNAME = "John.Doe";
    private static final String JTI = "jti-uuid";

    @Mock private JwtService jwtService;
    @Mock private TokenBlacklistService tokenBlacklistService;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private FilterChain chain;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilter_shouldAuthenticateForValidToken() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer " + TOKEN);
        when(jwtService.extractJti(TOKEN)).thenReturn(JTI);
        when(tokenBlacklistService.isInvalidated(JTI)).thenReturn(false);
        when(jwtService.extractUsername(TOKEN)).thenReturn(USERNAME);
        when(jwtService.extractRole(TOKEN)).thenReturn(Role.TRAINEE);

        filter.doFilter(request, response, chain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals(USERNAME, auth.getPrincipal());
        assertTrue(auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_TRAINEE")));
        verify(chain).doFilter(request, response);
    }

    @Test
    void doFilter_shouldNotAuthenticateWhenNoHeader() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        filter.doFilter(request, response, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(chain).doFilter(request, response);
        verifyNoInteractions(jwtService);
    }

    @Test
    void doFilter_shouldNotAuthenticateWhenHeaderNotBearer() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Basic abc");

        filter.doFilter(request, response, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(jwtService);
    }

    @Test
    void doFilter_shouldRejectInvalidatedToken() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer " + TOKEN);
        when(jwtService.extractJti(TOKEN)).thenReturn(JTI);
        when(tokenBlacklistService.isInvalidated(JTI)).thenReturn(true);

        filter.doFilter(request, response, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(jwtService, never()).extractUsername(any());
        verify(chain).doFilter(request, response);
    }

    @Test
    void doFilter_shouldNotAuthenticateOnJwtException() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer " + TOKEN);
        when(jwtService.extractJti(TOKEN)).thenThrow(new JwtException("invalid"));

        filter.doFilter(request, response, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(chain).doFilter(request, response);
    }
}