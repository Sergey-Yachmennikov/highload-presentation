package com.highload.highload_presentation.cpu;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PrimeCalculatorTest {

    private final PrimeCalculator calculator = new PrimeCalculator();

    private static final int N = 1_000_000;

    @Test
    void countPrimes_unoptimized_takesAbout5Seconds() {
        long start = System.currentTimeMillis();
        int result = calculator.countPrimesUnoptimized(N);
        long elapsed = System.currentTimeMillis() - start;

        assertEquals(78_498, result);
        System.out.println("Unoptimized: " + elapsed + " ms");
        assertTrue(elapsed >= 4000, "Expected at least 4s, got " + elapsed + " ms");
    }

    @Test
    void countPrimes_optimized_completesQuickly() {
        long start = System.currentTimeMillis();
        int result = calculator.countPrimesOptimized(N);
        long elapsed = System.currentTimeMillis() - start;

        assertEquals(78_498, result);
        System.out.println("Optimized: " + elapsed + " ms");
        assertTrue(elapsed < 500, "Expected under 500ms, got " + elapsed + " ms");
    }
}
