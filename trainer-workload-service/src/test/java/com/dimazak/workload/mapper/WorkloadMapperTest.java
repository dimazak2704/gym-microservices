package com.dimazak.workload.mapper;

import com.dimazak.workload.dto.TrainerSummaryResponse;
import com.dimazak.workload.entity.TrainerWorkload;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WorkloadMapperTest {

    private static final String USERNAME = "Jane.Smith";

    private final WorkloadMapper mapper = new WorkloadMapper();

    private TrainerWorkload row(int year, int month, int duration) {
        return TrainerWorkload.builder()
                .username(USERNAME).firstName("Jane").lastName("Smith")
                .active(true).year(year).month(month).totalDuration(duration).build();
    }

    @Test
    void toResponse_shouldReturnEmptyWhenNoRows() {
        TrainerSummaryResponse response = mapper.toResponse(USERNAME, List.of());

        assertEquals(USERNAME, response.username());
        assertNull(response.firstName());
        assertFalse(response.active());
        assertTrue(response.years().isEmpty());
    }

    @Test
    void toResponse_shouldGroupByYearAndSortMonths() {
        List<TrainerWorkload> rows = List.of(
                row(2024, 5, 90),
                row(2024, 4, 120),
                row(2023, 12, 45));

        TrainerSummaryResponse response = mapper.toResponse(USERNAME, rows);

        assertEquals(2, response.years().size());
        // роки відсортовані за зростанням → 2023 перший
        assertEquals(2023, response.years().get(0).year());
        assertEquals(2024, response.years().get(1).year());
        // місяці 2024 відсортовані → квітень(4) перед травнем(5)
        var months2024 = response.years().get(1).months();
        assertEquals(4, months2024.get(0).month());
        assertEquals(120, months2024.get(0).totalDuration());
        assertEquals(5, months2024.get(1).month());
    }

    @Test
    void toResponse_shouldTakeTrainerInfoFromFirstRow() {
        TrainerSummaryResponse response = mapper.toResponse(USERNAME, List.of(row(2024, 4, 60)));

        assertEquals("Jane", response.firstName());
        assertEquals("Smith", response.lastName());
        assertTrue(response.active());
    }
}