package com.dimazak.workload.service;

import com.dimazak.workload.document.MonthSummary;
import com.dimazak.workload.document.TrainerWorkloadDocument;
import com.dimazak.workload.document.YearSummary;
import com.dimazak.workload.dto.ActionType;
import com.dimazak.workload.dto.TrainerSummaryResponse;
import com.dimazak.workload.dto.WorkloadRequest;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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

    private TrainerWorkloadDocument docWith(int total) {
        MonthSummary m = MonthSummary.builder().month(MONTH).totalDuration(total).build();
        YearSummary y = YearSummary.builder().year(YEAR)
                .months(new ArrayList<>(List.of(m))).build();
        return TrainerWorkloadDocument.builder()
                .id("1").username(USERNAME).firstName(FIRST).lastName(LAST)
                .active(true).years(new ArrayList<>(List.of(y))).build();
    }

    // ==================== ADD ====================

    @Test
    void processWorkload_add_shouldCreateNewDocumentWhenNoneExists() {
        when(repository.findByUsername(USERNAME)).thenReturn(Optional.empty());

        service.processWorkload(request(ActionType.ADD, DURATION));

        ArgumentCaptor<TrainerWorkloadDocument> captor =
                ArgumentCaptor.forClass(TrainerWorkloadDocument.class);
        verify(repository).save(captor.capture());

        TrainerWorkloadDocument saved = captor.getValue();
        assertEquals(USERNAME, saved.getUsername());
        assertEquals(1, saved.getYears().size());
        assertEquals(YEAR, saved.getYears().get(0).getYear());
        assertEquals(DURATION, saved.getYears().get(0).getMonths().get(0).getTotalDuration());
        verify(metrics).incrementAdd();
    }

    @Test
    void processWorkload_add_shouldAccumulateWhenMonthExists() {
        TrainerWorkloadDocument doc = docWith(120);
        when(repository.findByUsername(USERNAME)).thenReturn(Optional.of(doc));

        service.processWorkload(request(ActionType.ADD, 30));

        assertEquals(150, doc.getYears().get(0).getMonths().get(0).getTotalDuration());
        verify(repository).save(doc);
        verify(metrics).incrementAdd();
    }

    @Test
    void processWorkload_add_shouldAddNewMonthToExistingYear() {
        TrainerWorkloadDocument doc = docWith(60); // has month 4
        WorkloadRequest mayRequest = new WorkloadRequest(
                USERNAME, FIRST, LAST, true, LocalDate.of(2024, 5, 1), 45, ActionType.ADD);
        when(repository.findByUsername(USERNAME)).thenReturn(Optional.of(doc));

        service.processWorkload(mayRequest);

        assertEquals(2, doc.getYears().get(0).getMonths().size());
    }

    @Test
    void processWorkload_add_shouldUpdateTrainerInfo() {
        TrainerWorkloadDocument doc = docWith(60);
        doc.setActive(false);
        when(repository.findByUsername(USERNAME)).thenReturn(Optional.of(doc));

        service.processWorkload(request(ActionType.ADD, 30));

        assertTrue(doc.isActive());
        assertEquals(FIRST, doc.getFirstName());
    }

    // ==================== DELETE ====================

    @Test
    void processWorkload_delete_shouldDecreaseWhenEnough() {
        TrainerWorkloadDocument doc = docWith(120);
        when(repository.findByUsername(USERNAME)).thenReturn(Optional.of(doc));

        service.processWorkload(request(ActionType.DELETE, 30));

        assertEquals(90, doc.getYears().get(0).getMonths().get(0).getTotalDuration());
        verify(repository).save(doc);
        verify(metrics).incrementDelete();
    }

    @Test
    void processWorkload_delete_shouldRemoveMonthWhenReachesZero() {
        TrainerWorkloadDocument doc = docWith(30);
        when(repository.findByUsername(USERNAME)).thenReturn(Optional.of(doc));

        service.processWorkload(request(ActionType.DELETE, 30));

        assertTrue(doc.getYears().isEmpty());
        verify(repository).save(doc);
    }

    @Test
    void processWorkload_delete_shouldRemoveMonthWhenGoesNegative() {
        TrainerWorkloadDocument doc = docWith(20);
        when(repository.findByUsername(USERNAME)).thenReturn(Optional.of(doc));

        service.processWorkload(request(ActionType.DELETE, 50));

        assertTrue(doc.getYears().isEmpty());
    }

    @Test
    void processWorkload_delete_shouldIgnoreWhenNoDocument() {
        when(repository.findByUsername(USERNAME)).thenReturn(Optional.empty());

        service.processWorkload(request(ActionType.DELETE, 30));

        verify(repository, never()).save(any());
        verify(metrics).incrementDelete();
    }

    @Test
    void processWorkload_delete_shouldIgnoreWhenNoMatchingMonth() {
        TrainerWorkloadDocument doc = docWith(60); // month 4
        WorkloadRequest mayDelete = new WorkloadRequest(
                USERNAME, FIRST, LAST, true, LocalDate.of(2024, 5, 1), 30, ActionType.DELETE);
        when(repository.findByUsername(USERNAME)).thenReturn(Optional.of(doc));

        service.processWorkload(mayDelete);

        verify(repository, never()).save(any());
        verify(metrics).incrementDelete();
    }

    // ==================== getSummary ====================

    @Test
    void getSummary_shouldDelegateToMapper() {
        TrainerWorkloadDocument doc = docWith(60);
        when(repository.findByUsername(USERNAME)).thenReturn(Optional.of(doc));
        TrainerSummaryResponse expected =
                new TrainerSummaryResponse(USERNAME, FIRST, LAST, true, List.of());
        when(mapper.toResponse(USERNAME, doc)).thenReturn(expected);

        TrainerSummaryResponse result = service.getSummary(USERNAME, null, null);

        assertEquals(expected, result);
    }

    @Test
    void getSummary_shouldReturnEmptyWhenNoDocument() {
        when(repository.findByUsername(USERNAME)).thenReturn(Optional.empty());
        TrainerSummaryResponse expected =
                new TrainerSummaryResponse(USERNAME, null, null, false, List.of());
        when(mapper.toResponse(USERNAME, null)).thenReturn(expected);

        TrainerSummaryResponse result = service.getSummary(USERNAME, null, null);

        assertEquals(expected, result);
    }

    @Test
    void getSummary_shouldFilterWithoutMutatingStoredDocument() {
        TrainerWorkloadDocument doc = docWith(60);
        doc.getYears().add(YearSummary.builder()
                .year(2025)
                .months(new ArrayList<>(List.of(MonthSummary.builder()
                        .month(1).totalDuration(30).build())))
                .build());
        when(repository.findByUsername(USERNAME)).thenReturn(Optional.of(doc));

        service.getSummary(USERNAME, YEAR, MONTH);

        ArgumentCaptor<TrainerWorkloadDocument> captor =
                ArgumentCaptor.forClass(TrainerWorkloadDocument.class);
        verify(mapper).toResponse(eq(USERNAME), captor.capture());
        assertEquals(1, captor.getValue().getYears().size());
        assertEquals(YEAR, captor.getValue().getYears().get(0).getYear());
        assertEquals(2, doc.getYears().size());
        assertEquals(1, doc.getYears().get(0).getMonths().size());
    }
}
