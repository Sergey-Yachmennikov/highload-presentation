package com.highload.highload_presentation.concurrency;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

class ParallelExamplesTest {

    private ParallelExamples examples;

    @BeforeEach
    void setUp() {
        examples = new ParallelExamples();
    }

    // ── Параллельное суммирование ──────────────────

    @Test
    void parallelSum_correctForSmallArray() throws ExecutionException, InterruptedException {
        long[] array = {1, 2, 3, 4, 5, 6, 7, 8};
        assertEquals(36, examples.parallelSum(array));
    }

    @Test
    void parallelSum_correctForLargeArray() throws ExecutionException, InterruptedException {
        long[] array = new long[10_000];
        for (int i = 0; i < array.length; i++) array[i] = i + 1;
        long expected = (long) 10_000 * 10_001 / 2;
        assertEquals(expected, examples.parallelSum(array));
    }

    @Test
    void parallelSumStream_matchesSequential() throws ExecutionException, InterruptedException {
        long[] array = new long[1_000];
        for (int i = 0; i < array.length; i++) array[i] = i + 1;

        long sequential = examples.parallelSum(array);
        long parallel = examples.parallelSumStream(array);
        assertEquals(sequential, parallel);
    }

    // ── Параллельная обработка элементов ──────────

    @Test
    void processItemsInParallel_transformsAllItems() {
        List<String> input = List.of("hello", "world", "java");
        List<String> result = examples.processItemsInParallel(input);

        assertEquals(3, result.size());
        assertTrue(result.contains("HELLOHELLO"));
        assertTrue(result.contains("WORLDWORLD"));
        assertTrue(result.contains("JAVAJAVA"));
    }

    @Test
    void processItemsInParallel_emptyListReturnsEmptyList() {
        List<String> result = examples.processItemsInParallel(List.of());
        assertTrue(result.isEmpty());
    }

    // ── Параллельные I/O запросы ───────────────────

    @Test
    void fetchAllInParallel_returnsResponseForEachUrl() throws InterruptedException {
        List<String> urls = List.of("http://service-a", "http://service-b", "http://service-c");
        List<String> results = examples.fetchAllInParallel(urls);

        assertEquals(3, results.size());
        assertTrue(results.get(0).contains("service-a"));
        assertTrue(results.get(1).contains("service-b"));
        assertTrue(results.get(2).contains("service-c"));
    }

    @Test
    void fetchAllInParallel_emptyListReturnsEmptyList() throws InterruptedException {
        List<String> results = examples.fetchAllInParallel(List.of());
        assertTrue(results.isEmpty());
    }

    // ── ForkJoin Fibonacci ─────────────────────────

    @Test
    void forkJoinFibonacci_baseCase() {
        assertEquals(0, examples.forkJoinFibonacci(0));
        assertEquals(1, examples.forkJoinFibonacci(1));
    }

    @Test
    void forkJoinFibonacci_correctValues() {
        assertEquals(8,   examples.forkJoinFibonacci(6));
        assertEquals(55,  examples.forkJoinFibonacci(10));
        assertEquals(144, examples.forkJoinFibonacci(12));
    }

    @Test
    void forkJoinFibonacci_largeValue() {
        // Просто проверяем, что завершается без ошибок
        assertDoesNotThrow(() -> examples.forkJoinFibonacci(30));
    }
}
