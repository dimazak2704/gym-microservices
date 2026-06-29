package com.dimazak.gym.client;

import com.dimazak.gym.dto.WorkloadRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "trainer-workload-service")
public interface WorkloadClient {

    @PostMapping("/api/workload")
    void sendWorkload(@RequestBody WorkloadRequest request,
                      @RequestHeader("Authorization") String authToken,
                      @RequestHeader("X-Transaction-Id") String transactionId);
}