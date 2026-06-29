package com.dimazak.gym.client;

import com.dimazak.gym.dto.WorkloadRequest;
import com.dimazak.gym.security.TokenProvider;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Component
public class WorkloadCircuitBreakerClient {

    private static final Logger log =
            LoggerFactory.getLogger(WorkloadCircuitBreakerClient.class);
    private static final String CB_NAME = "workloadService";

    private final WorkloadClient workloadClient;
    private final TokenProvider tokenProvider;

    public WorkloadCircuitBreakerClient(WorkloadClient workloadClient,
                                        TokenProvider tokenProvider) {
        this.workloadClient = workloadClient;
        this.tokenProvider = tokenProvider;
    }

    @CircuitBreaker(name = CB_NAME, fallbackMethod = "sendFallback")
    public void send(WorkloadRequest request) {
        String token = tokenProvider.getCurrentToken();
        String txId = MDC.get("transactionId");
        log.info("Sending workload: trainer='{}', action={}, txId={}",
                request.trainerUsername(), request.actionType(), txId);
        workloadClient.sendWorkload(request, token, txId);
        log.info("Workload sent for trainer '{}'", request.trainerUsername());
    }

    private void sendFallback(WorkloadRequest request, Throwable t) {
        log.error("Report service unavailable for '{}' [txId={}]: {}. " +
                        "Main operation succeeded; report NOT updated.",
                request.trainerUsername(), MDC.get("transactionId"), t.getMessage());
        //TODO implement a retry mechanism or queue the request for later processing
    }
}