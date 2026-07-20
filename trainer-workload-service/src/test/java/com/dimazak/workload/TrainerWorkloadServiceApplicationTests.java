package com.dimazak.workload;

import com.dimazak.workload.repository.TrainerWorkloadRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
class TrainerWorkloadServiceApplicationTests {

    @MockitoBean
    private TrainerWorkloadRepository repository;

    @Test
    void contextLoads() {
    }
}