package com.dimazak.gym.health;

import org.junit.jupiter.api.Test;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.Status;

import static org.junit.jupiter.api.Assertions.*;

class DiskSpaceHealthIndicatorTest {

    private final DiskSpaceHealthIndicator indicator = new DiskSpaceHealthIndicator();

    @Test
    void health_shouldReturnUpWhenEnoughSpace() {
        Health health = indicator.health();

        assertEquals(Status.UP, health.getStatus());
        assertNotNull(health.getDetails().get("freeBytes"));
        assertTrue((long) health.getDetails().get("freeBytes") > 0);
    }

    @Test
    void health_shouldContainFreeBytesDetail() {
        Health health = indicator.health();

        assertTrue(health.getDetails().containsKey("freeBytes"));
        Object freeBytes = health.getDetails().get("freeBytes");
        assertInstanceOf(Long.class, freeBytes);
    }
}