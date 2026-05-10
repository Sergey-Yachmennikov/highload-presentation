package com.highload.highload_presentation.cpu;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ParallelSumTest {

    private final ParallelSum parallelSum = new ParallelSum();

    private static int[] array;

    @BeforeAll
    static void setUp() {
        Random random = new Random(42);
        array = new int[200_000];
        for (int i = 0; i < array.length; i++) {
            array[i] = random.nextInt(1000);
        }
    }

    @Test
    void sum_singleThread_slow() {
        long start = System.currentTimeMillis();
        long result = parallelSum.sumSingleThread(array);
        long elapsed = System.currentTimeMillis() - start;

        System.out.println("Single thread: " + elapsed + " ms, result: " + result);
        assertTrue(elapsed >= 2000, "Expected at least 2s, got " + elapsed + " ms");
    }

    @Test
    void sum_parallel_fast() {
        long start = System.currentTimeMillis();
        long result = parallelSum.sumParallel(array);
        long elapsed = System.currentTimeMillis() - start;

        System.out.println("Parallel: " + elapsed + " ms, result: " + result);
        assertTrue(elapsed < 2000, "Expected under 2s, got " + elapsed + " ms");
    }

    @Test
    void sum_bothReturnSameResult() {
        int[] smallArray = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        long singleResult = parallelSum.sumSingleThread(smallArray);
        long parallelResult = parallelSum.sumParallel(smallArray);
        assertEquals(singleResult, parallelResult);
    }
}
