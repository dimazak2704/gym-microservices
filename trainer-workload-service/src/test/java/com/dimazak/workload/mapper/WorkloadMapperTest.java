package com.dimazak.workload.mapper;

import com.dimazak.workload.document.MonthSummary;
import com.dimazak.workload.document.TrainerWorkloadDocument;
import com.dimazak.workload.document.YearSummary;
import com.dimazak.workload.dto.TrainerSummaryResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WorkloadMapperTest {

    private static final String USERNAME = "Jane.Smith";

    private final WorkloadMapper mapper = new WorkloadMapper();

    private TrainerWorkloadDocument buildDoc(YearSummary... years) {
        return TrainerWorkloadDocument.builder()
                .username(USERNAME).firstName("Jane").lastName("Smith")
                .active(true).years(List.of(years)).build();
    }

    private YearSummary year(int y, MonthSummary... months) {
        return YearSummary.builder().year(y).months(List.of(months)).build();
    }

    private MonthSummary month(int m, int d) {
        return MonthSummary.builder().month(m).totalDuration(d).build();
    }

    @Test
    void toResponse_shouldReturnEmptyWhenNull() {
        TrainerSummaryResponse response = mapper.toResponse(USERNAME, null);

        assertEquals(USERNAME, response.username());
        assertNull(response.firstName());
        assertFalse(response.active());
        assertTrue(response.years().isEmpty());
    }

    @Test
    void toResponse_shouldMapTrainerInfo() {
        TrainerWorkloadDocument doc = buildDoc(year(2024, month(4, 60)));

        TrainerSummaryResponse response = mapper.toResponse(USERNAME, doc);

        assertEquals("Jane", response.firstName());
        assertEquals("Smith", response.lastName());
        assertTrue(response.active());
    }

    @Test
    void toResponse_shouldSortYearsAndMonths() {
        TrainerWorkloadDocument doc = buildDoc(
                year(2024, month(5, 90), month(4, 120)),
                year(2023, month(12, 45)));

        TrainerSummaryResponse response = mapper.toResponse(USERNAME, doc);

        assertEquals(2, response.years().size());
        assertEquals(2023, response.years().get(0).year());
        assertEquals(2024, response.years().get(1).year());
        var months2024 = response.years().get(1).months();
        assertEquals(4, months2024.get(0).month());
        assertEquals(120, months2024.get(0).totalDuration());
        assertEquals(5, months2024.get(1).month());
    }

    @Test
    void toResponse_shouldHandleEmptyYears() {
        TrainerWorkloadDocument doc = buildDoc();

        TrainerSummaryResponse response = mapper.toResponse(USERNAME, doc);

        assertTrue(response.years().isEmpty());
    }
}