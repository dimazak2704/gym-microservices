package com.dimazak.workload.service;

import com.dimazak.workload.dto.ActionType;
import com.dimazak.workload.dto.TrainerSummaryResponse;
import com.dimazak.workload.dto.WorkloadRequest;
import com.dimazak.workload.entity.TrainerWorkload;
import com.dimazak.workload.mapper.WorkloadMapper;
import com.dimazak.workload.metrics.WorkloadMetrics;
import com.dimazak.workload.repository.TrainerWorkloadRepository;
import com.dimazak.workload.service.impl.WorkloadServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkloadServiceImplTest {

    private static final String USERNAME = "Jane.Smith";
    private static final String FIRST = "Jane";
    private static final String LAST = "Smith";
    private static final LocalDate DATE = LocalDate.of(2024, 4, 1);
    private static final int YEAR = 2024;
    private static final int MONTH = 4;
    private static final int DURATION = 60;

    @Mock private TrainerWorkloadRepository repository;
    @Mock private WorkloadMapper mapper;
    @Mock private WorkloadMetrics metrics;

    @InjectMocks private WorkloadServiceImpl service;

    private WorkloadRequest request(ActionType action, int duration) {
        return new WorkloadRequest(USERNAME, FIRST, LAST, true, DATE, duration, action);
    }

    private TrainerWorkload existing(int total) {
        return TrainerWorkload.builder()
                .id(1L).username(USERNAME).firstName(FIRST).lastName(LAST)
                .active(true).year(YEAR).month(MONTH).totalDuration(total).build();
    }

    // ==================== ADD ====================

    @Test
    void processWorkload_add_shouldCreateNewWhenNoExisting() {
        when(repository.findByUsernameAndYearAndMonth(USERNAME, YEAR, MONTH))
                .thenReturn(Optional.empty());

        service.processWorkload(request(ActionType.ADD, DURATION));

        ArgumentCaptor<TrainerWorkload> captor = ArgumentCaptor.forClass(TrainerWorkload.class);
        verify(repository).save(captor.capture());
        assertEquals(DURATION, captor.getValue().getTotalDuration());
        assertEquals(USERNAME, captor.getValue().getUsername());
        verify(metrics).incrementAdd();
    }

    @Test
    void processWorkload_add_shouldAccumulateWhenExisting() {
        TrainerWorkload existing = existing(120);
        when(repository.findByUsernameAndYearAndMonth(USERNAME, YEAR, MONTH))
                .thenReturn(Optional.of(existing));

        service.processWorkload(request(ActionType.ADD, 30));

        assertEquals(150, existing.getTotalDuration());
        verify(repository).save(existing);
        verify(metrics).incrementAdd();
    }

    @Test
    void processWorkload_add_shouldUpdateNameAndStatusWhenExisting() {
        TrainerWorkload existing = existing(60);
        existing.setActive(false);
        when(repository.findByUsernameAndYearAndMonth(USERNAME, YEAR, MONTH))
                .thenReturn(Optional.of(existing));

        service.processWorkload(request(ActionType.ADD, 30));

        assertTrue(existing.isActive());
        assertEquals(FIRST, existing.getFirstName());
    }

    // ==================== DELETE ====================

    @Test
    void processWorkload_delete_shouldDecreaseWhenEnough() {
        TrainerWorkload existing = existing(120);
        when(repository.findByUsernameAndYearAndMonth(USERNAME, YEAR, MONTH))
                .thenReturn(Optional.of(existing));

        service.processWorkload(request(ActionType.DELETE, 30));

        assertEquals(90, existing.getTotalDuration());
        verify(repository).save(existing);
        verify(metrics).incrementDelete();
    }

    @Test
    void processWorkload_delete_shouldRemoveRowWhenReachesZero() {
        TrainerWorkload existing = existing(30);
        when(repository.findByUsernameAndYearAndMonth(USERNAME, YEAR, MONTH))
                .thenReturn(Optional.of(existing));

        service.processWorkload(request(ActionType.DELETE, 30));

        verify(repository).delete(existing);
        verify(repository, never()).save(any());
    }

    @Test
    void processWorkload_delete_shouldRemoveRowWhenGoesNegative() {
        TrainerWorkload existing = existing(20);
        when(repository.findByUsernameAndYearAndMonth(USERNAME, YEAR, MONTH))
                .thenReturn(Optional.of(existing));

        service.processWorkload(request(ActionType.DELETE, 50));

        verify(repository).delete(existing);
    }

    @Test
    void processWorkload_delete_shouldIgnoreWhenNoRecord() {
        when(repository.findByUsernameAndYearAndMonth(USERNAME, YEAR, MONTH))
                .thenReturn(Optional.empty());

        service.processWorkload(request(ActionType.DELETE, 30));

        verify(repository, never()).save(any());
        verify(repository, never()).delete(any());
        verify(metrics).incrementDelete();
    }

    // ==================== getSummary ====================

    @Test
    void getSummary_shouldDelegateToMapper() {
        List<TrainerWorkload> rows = List.of(existing(60));
        when(repository.findByUsername(USERNAME)).thenReturn(rows);
        TrainerSummaryResponse expected =
                new TrainerSummaryResponse(USERNAME, FIRST, LAST, true, List.of());
        when(mapper.toResponse(USERNAME, rows)).thenReturn(expected);

        TrainerSummaryResponse result = service.getSummary(USERNAME);

        assertEquals(expected, result);
        verify(mapper).toResponse(USERNAME, rows);
    }
}