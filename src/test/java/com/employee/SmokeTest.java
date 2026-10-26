package com.employee;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** Proves that JUnit 5 is wired correctly. Real tests start in Phase 3. */
class SmokeTest {

    @Test
    void javaVersionIsAtLeast21() {
        int version = Runtime.version().feature();
        assertTrue(version >= 21, "Expected Java 21+, but found " + version);
    }
}