package com.highload.highload_presentation.memory;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoopInitializationTest {

    private final LoopInitialization loopInit = new LoopInitialization();


    @Test
    void process_withReallocation_slow() {
        long start = System.currentTimeMillis();
        long result = loopInit.processWithReallocation(1_000_000);
        long elapsed = System.currentTimeMillis() - start;

        assertEquals(499_999_500_000L, result);
        System.out.println("With reallocation: " + elapsed + " ms");
    }

    @Test
    void process_withReuse_fast() {
        long start = System.currentTimeMillis();
        long result = loopInit.processWithReuse(1_000_000);
        long elapsed = System.currentTimeMillis() - start;

        assertEquals(499_999_500_000L, result);
        System.out.println("With reuse: " + elapsed + " ms");
        assertTrue(elapsed < 100, "Expected under 100ms, got " + elapsed + " ms");
    }
}
