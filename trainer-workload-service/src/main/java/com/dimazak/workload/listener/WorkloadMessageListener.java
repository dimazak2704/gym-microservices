package com.dimazak.workload.listener;

import com.dimazak.workload.dto.WorkloadRequest;
import com.dimazak.workload.service.WorkloadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Component
@Validated
@RequiredArgsConstructor
public class WorkloadMessageListener {

    public static final String TRAINING_QUEUE = "training.queue";
    private static final String TX_HEADER = "transactionId";

    private final WorkloadService workloadService;

    @JmsListener(destination = TRAINING_QUEUE, concurrency = "3-10")
    public void receive(@Valid @Payload WorkloadRequest request,
                        @Header(name = TX_HEADER, required = false) String transactionId) {
        if (transactionId != null) {
            MDC.put(TX_HEADER, transactionId);
        }
        try {
            log.info("Received workload message: trainer='{}', action={}, txId={}",
                    request.trainerUsername(), request.actionType(), transactionId);

            workloadService.processWorkload(request);

            log.info("Workload message processed for trainer '{}'", request.trainerUsername());
        } finally {
            MDC.remove(TX_HEADER);
        }
    }
}