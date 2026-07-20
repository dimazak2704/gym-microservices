package com.dimazak.workload.listener;

import com.dimazak.workload.dto.ActionType;
import com.dimazak.workload.dto.WorkloadRequest;
import com.dimazak.workload.service.WorkloadService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkloadMessageListenerTest {

    private static final String USERNAME = "Jane.Smith";
    private static final String TX_ID = "test-tx-id-123";
    private static final LocalDate DATE = LocalDate.of(2024, 4, 15);

    @Mock
    private WorkloadService workloadService;

    @InjectMocks
    private WorkloadMessageListener listener;

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    private WorkloadRequest validRequest(ActionType action) {
        return new WorkloadRequest(USERNAME, "Jane", "Smith", true, DATE, 60, action);
    }

    @Test
    void receive_shouldDelegateToService() {
        WorkloadRequest request = validRequest(ActionType.ADD);

        listener.receive(request, TX_ID);

        verify(workloadService).processWorkload(request);
    }

    @Test
    void receive_shouldSetTransactionIdInMDC() {
        WorkloadRequest request = validRequest(ActionType.ADD);

        // During execution, MDC should have the transactionId
        doAnswer(inv -> {
            assertEquals(TX_ID, MDC.get("transactionId"));
            return null;
        }).when(workloadService).processWorkload(any());

        listener.receive(request, TX_ID);
    }

    @Test
    void receive_shouldClearMDCAfterProcessing() {
        WorkloadRequest request = validRequest(ActionType.ADD);

        listener.receive(request, TX_ID);

        assertNull(MDC.get("transactionId"));
    }

    @Test
    void receive_shouldClearMDCEvenOnException() {
        WorkloadRequest request = validRequest(ActionType.ADD);
        doThrow(new RuntimeException("db error")).when(workloadService).processWorkload(any());

        assertThrows(RuntimeException.class, () -> listener.receive(request, TX_ID));

        assertNull(MDC.get("transactionId"));
    }

    @Test
    void receive_shouldHandleNullTransactionId() {
        WorkloadRequest request = validRequest(ActionType.DELETE);
        doAnswer(invocation -> {
            assertNotNull(MDC.get("transactionId"));
            return null;
        }).when(workloadService).processWorkload(request);

        listener.receive(request, null);

        verify(workloadService).processWorkload(request);
        assertNull(MDC.get("transactionId"));
    }

    @Test
    void receive_shouldProcessDeleteAction() {
        WorkloadRequest request = validRequest(ActionType.DELETE);

        listener.receive(request, TX_ID);

        verify(workloadService).processWorkload(request);
    }
}
