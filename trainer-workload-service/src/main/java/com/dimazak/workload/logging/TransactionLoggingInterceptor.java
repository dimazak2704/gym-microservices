package com.dimazak.workload.logging;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

@Slf4j
@Component
public class TransactionLoggingInterceptor implements HandlerInterceptor {

    private static final String TRANSACTION_ID_KEY = "transactionId";
    private static final String TRANSACTION_ID_HEADER = "X-Transaction-Id";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) {
        String transactionId = request.getHeader(TRANSACTION_ID_HEADER);
        if (transactionId == null || transactionId.isBlank()) {
            transactionId = UUID.randomUUID().toString();
        }
        MDC.put(TRANSACTION_ID_KEY, transactionId);

        log.info("Transaction [{}] START | {} {} | RemoteAddr: {}",
                transactionId, request.getMethod(), request.getRequestURI(),
                request.getRemoteAddr());

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