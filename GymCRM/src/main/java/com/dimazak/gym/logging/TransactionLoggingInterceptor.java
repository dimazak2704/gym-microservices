package com.dimazak.gym.logging;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

@Component
public class TransactionLoggingInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(TransactionLoggingInterceptor.class);
    private static final String TRANSACTION_ID_KEY = "transactionId";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) {
        String transactionId = UUID.randomUUID().toString();
        MDC.put(TRANSACTION_ID_KEY, transactionId);

        log.info("Transaction [{}] START | {} {} | RemoteAddr: {}",
                transactionId, request.getMethod(), request.getRequestURI(),
                request.getRemoteAddr());

        if (request.getQueryString() != null) {
            log.debug("Transaction [{}] Query params: {}", transactionId, request.getQueryString());
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        String transactionId = MDC.get(TRANSACTION_ID_KEY);
        int status = response.getStatus();

        if (ex != null) {
            log.error("Transaction [{}] END | {} {} | Status: {} | Error: {}",
                    transactionId, request.getMethod(), request.getRequestURI(),
                    status, ex.getMessage());
        } else {
            log.info("Transaction [{}] END | {} {} | Status: {}",
                    transactionId, request.getMethod(), request.getRequestURI(), status);
        }

        MDC.remove(TRANSACTION_ID_KEY);
    }
}