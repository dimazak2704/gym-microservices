package com.dimazak.gym.logging;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransactionLoggingInterceptorTest {

    private static final String TRANSACTION_ID_KEY = "transactionId";
    private static final String GET_METHOD = "GET";
    private static final String POST_METHOD = "POST";
    private static final String TEST_URI = "/api/trainees/John.Doe";
    private static final String LOGIN_URI = "/api/users/login";
    private static final String TRAININGS_URI = "/api/trainings";
    private static final String LOCALHOST = "127.0.0.1";
    private static final String QUERY_STRING = "username=John.Doe";
    private static final String TEST_TRANSACTION_ID = "test-id-123";
    private static final int STATUS_OK = 200;
    private static final int STATUS_ERROR = 500;
    private static final int UUID_LENGTH = 36;

    private final TransactionLoggingInterceptor interceptor = new TransactionLoggingInterceptor();

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void preHandle_shouldSetTransactionIdInMDC() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getMethod()).thenReturn(GET_METHOD);
        when(request.getRequestURI()).thenReturn(TEST_URI);
        when(request.getRemoteAddr()).thenReturn(LOCALHOST);

        boolean result = interceptor.preHandle(request, response, new Object());

        assertTrue(result);
        assertNotNull(MDC.get(TRANSACTION_ID_KEY));
        assertEquals(UUID_LENGTH, MDC.get(TRANSACTION_ID_KEY).length());
    }

    @Test
    void preHandle_shouldHandleQueryParams() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getMethod()).thenReturn(GET_METHOD);
        when(request.getRequestURI()).thenReturn(LOGIN_URI);
        when(request.getRemoteAddr()).thenReturn(LOCALHOST);
        when(request.getQueryString()).thenReturn(QUERY_STRING);

        boolean result = interceptor.preHandle(request, response, new Object());

        assertTrue(result);
        assertNotNull(MDC.get(TRANSACTION_ID_KEY));
    }

    @Test
    void preHandle_shouldHandleNullQueryString() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getMethod()).thenReturn(GET_METHOD);
        when(request.getRequestURI()).thenReturn(TEST_URI);
        when(request.getRemoteAddr()).thenReturn(LOCALHOST);
        when(request.getQueryString()).thenReturn(null);

        boolean result = interceptor.preHandle(request, response, new Object());

        assertTrue(result);
    }

    @Test
    void afterCompletion_shouldClearMDC() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getMethod()).thenReturn(GET_METHOD);
        when(request.getRequestURI()).thenReturn(TEST_URI);
        when(response.getStatus()).thenReturn(STATUS_OK);
        MDC.put(TRANSACTION_ID_KEY, TEST_TRANSACTION_ID);

        interceptor.afterCompletion(request, response, new Object(), null);

        assertNull(MDC.get(TRANSACTION_ID_KEY));
    }

    @Test
    void afterCompletion_shouldHandleException() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getMethod()).thenReturn(POST_METHOD);
        when(request.getRequestURI()).thenReturn(TRAININGS_URI);
        when(response.getStatus()).thenReturn(STATUS_ERROR);
        MDC.put(TRANSACTION_ID_KEY, TEST_TRANSACTION_ID);

        interceptor.afterCompletion(request, response, new Object(),
                new RuntimeException("Test error"));

        assertNull(MDC.get(TRANSACTION_ID_KEY));
    }

    @Test
    void afterCompletion_shouldHandleNullTransactionId() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getMethod()).thenReturn(GET_METHOD);
        when(request.getRequestURI()).thenReturn(TEST_URI);
        when(response.getStatus()).thenReturn(STATUS_OK);

        assertDoesNotThrow(() ->
                interceptor.afterCompletion(request, response, new Object(), null));
    }
}