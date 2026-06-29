package com.dimazak.gym.health;

import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class DiskSpaceHealthIndicator implements HealthIndicator {

    private static final long THRESHOLD_BYTES = 100 * 1024 * 1024;

    @Override
    public Health health() {
        File disk = new File(".");
        long free = disk.getUsableSpace();

        if (free < THRESHOLD_BYTES) {
            return Health.down()
                    .withDetail("freeBytes", free)
                    .withDetail("thresholdBytes", THRESHOLD_BYTES)
                    .withDetail("reason", "Not enough free disk space")
                    .build();
        }
        return Health.up()
                .withDetail("freeBytes", free)
                .build();
    }
}
