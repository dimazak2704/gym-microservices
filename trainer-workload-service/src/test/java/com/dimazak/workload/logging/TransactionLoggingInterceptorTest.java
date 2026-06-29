package com.dimazak.workload.logging;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransactionLoggingInterceptorTest {

    private static final String KEY = "transactionId";
    private static final String HEADER = "X-Transaction-Id";
    private static final String INCOMING_ID = "gym-crm-txid-123";

    private final TransactionLoggingInterceptor interceptor = new TransactionLoggingInterceptor();

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void preHandle_shouldReuseIncomingTransactionId() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getHeader(HEADER)).thenReturn(INCOMING_ID);
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/api/workload");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        interceptor.preHandle(request, response, new Object());

        assertEquals(INCOMING_ID, MDC.get(KEY));
    }

    @Test
    void preHandle_shouldGenerateIdWhenHeaderMissing() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getHeader(HEADER)).thenReturn(null);
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/workload/Jane.Smith");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        interceptor.preHandle(request, response, new Object());

        assertNotNull(MDC.get(KEY));
        assertEquals(36, MDC.get(KEY).length());
    }

    @Test
    void afterCompletion_shouldClearMDC() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/workload/x");
        when(response.getStatus()).thenReturn(200);
        MDC.put(KEY, INCOMING_ID);

        interceptor.afterCompletion(request, response, new Object(), null);

        assertNull(MDC.get(KEY));
    }

    @Test
    void afterCompletion_shouldHandleException() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/api/workload");
        when(response.getStatus()).thenReturn(500);
        MDC.put(KEY, INCOMING_ID);

        interceptor.afterCompletion(request, response, new Object(),
                new RuntimeException("boom"));

        assertNull(MDC.get(KEY));
    }
}