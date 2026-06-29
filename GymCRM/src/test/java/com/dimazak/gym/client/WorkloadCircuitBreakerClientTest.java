package com.dimazak.gym.client;

import com.dimazak.gym.dto.WorkloadActionType;
import com.dimazak.gym.dto.WorkloadRequest;
import com.dimazak.gym.security.TokenProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkloadCircuitBreakerClientTest {

    private static final String TOKEN = "Bearer abc.def.ghi";
    private static final String USERNAME = "Jane.Smith";

    @Mock private WorkloadClient workloadClient;
    @Mock private TokenProvider tokenProvider;

    @InjectMocks private WorkloadCircuitBreakerClient cbClient;

    private WorkloadRequest request() {
        return new WorkloadRequest(USERNAME, "Jane", "Smith", true,
                LocalDate.of(2024, 4, 1), 60, WorkloadActionType.ADD);
    }

    @Test
    void send_shouldCallFeignClientWithTokenAndTxId() {
        when(tokenProvider.getCurrentToken()).thenReturn(TOKEN);
        WorkloadRequest req = request();

        cbClient.send(req);

        verify(workloadClient).sendWorkload(eq(req), eq(TOKEN), any());
    }

    @Test
    void send_shouldPassNullTokenWhenNoneAvailable() {
        when(tokenProvider.getCurrentToken()).thenReturn(null);
        WorkloadRequest req = request();

        cbClient.send(req);

        verify(workloadClient).sendWorkload(eq(req), isNull(), any());
    }

    @Test
    void sendFallback_shouldNotThrow() {
        when(tokenProvider.getCurrentToken()).thenReturn(TOKEN);
        doThrow(new RuntimeException("service down"))
                .when(workloadClient).sendWorkload(any(), any(), any());

        assertThrows(RuntimeException.class, () -> cbClient.send(request()));
    }
}