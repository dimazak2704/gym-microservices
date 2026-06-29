package com.dimazak.workload.service;

import com.dimazak.workload.dto.TrainerSummaryResponse;
import com.dimazak.workload.dto.WorkloadRequest;

public interface WorkloadService {

    void processWorkload(WorkloadRequest request);

    TrainerSummaryResponse getSummary(String username);
}