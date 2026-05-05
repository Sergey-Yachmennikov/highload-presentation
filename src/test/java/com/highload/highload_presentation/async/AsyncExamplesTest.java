package com.highload.highload_presentation.async;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.*;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

class AsyncExamplesTest {

    private ExecutorService executor;
    private AsyncExamples examples;

    @BeforeEach
    void setUp() {
        executor = Executors.newFixedThreadPool(4);
        examples = new AsyncExamples(executor);
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
    }

    // ── 1. Future ─────────────────────────────────

    @Test
    void heavyComputationAsync_returnsCorrectResult() throws ExecutionException, InterruptedException {
        Future<Long> future = examples.heavyComputationAsync(100);

        // Главный поток может делать другую работу пока идёт вычисление
        long mainThreadWork = 1 + 1; // не блокируемся

        long result = future.get(); // только здесь ждём
        assertEquals(5050L, result);
        assertEquals(2, mainThreadWork);
    }

    @Test
    void heavyComputationAsync_runsInSeparateThread() throws ExecutionException, InterruptedException {
        String mainThread = Thread.currentThread().getName();

        Future<String> threadNameFuture = executor.submit(() -> Thread.currentThread().getName());

        assertNotEquals(mainThread, threadNameFuture.get());
    }

    @Test
    void heavyComputationAsync_zeroInput() throws ExecutionException, InterruptedException {
        assertEquals(0L, examples.heavyComputationAsync(0).get());
    }

    // ── 2. CompletableFuture — цепочка ────────────

    @Test
    void heavyComputationThenFormat_returnsFormattedResult() throws ExecutionException, InterruptedException {
        String result = examples.heavyComputationThenFormat(100).get();
        assertEquals("Результат вычисления: 5050", result);
    }

    @Test
    void heavyComputationThenFormat_doesNotBlockCallerThread() {
        long start = System.currentTimeMillis();

        // Запускаем асинхронно — не вызываем get() сразу
        CompletableFuture<String> future = examples.heavyComputationThenFormat(1_000_000);

        // Главный поток не заблокирован — проверяем время до get()
        long elapsed = System.currentTimeMillis() - start;
        assertTrue(elapsed < 500, "Вызов заблокировал главный поток на " + elapsed + "ms");

        assertDoesNotThrow(() -> future.get(5, TimeUnit.SECONDS));
    }

    // ── 3. allOf — все задачи параллельно ─────────

    @Test
    void runAllInParallel_returnsResultsForAllInputs() throws ExecutionException, InterruptedException {
        List<Long> results = examples.runAllInParallel(List.of(1, 2, 3, 4)).get();

        assertEquals(4, results.size());
        assertTrue(results.contains(1L));   // sum(1) = 1
        assertTrue(results.contains(3L));   // sum(2) = 3
        assertTrue(results.contains(6L));   // sum(3) = 6
        assertTrue(results.contains(10L));  // sum(4) = 10
    }

    @Test
    void runAllInParallel_emptyListReturnsEmptyResult() throws ExecutionException, InterruptedException {
        List<Long> results = examples.runAllInParallel(List.of()).get();
        assertTrue(results.isEmpty());
    }

    @Test
    void runAllInParallel_fasterThanSequential() throws ExecutionException, InterruptedException {
        // 4 задачи по 50ms каждая: последовательно ~200ms, параллельно ~50ms
        ExecutorService slowExecutor = Executors.newFixedThreadPool(4);
        AsyncExamples slowExamples = new AsyncExamples(slowExecutor) {
            @Override
            long simulateHeavyWork(int n) {
                try { Thread.sleep(50); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                return n;
            }
        };

        long start = System.currentTimeMillis();
        slowExamples.runAllInParallel(List.of(1, 2, 3, 4)).get();
        long elapsed = System.currentTimeMillis() - start;

        slowExecutor.shutdown();
        assertTrue(elapsed < 150, "Параллельное выполнение заняло " + elapsed + "ms, ожидалось < 150ms");
    }

    // ── 4. anyOf — первый готовый ─────────────────

    @Test
    void firstResult_returnsFirstCompletedValue() throws ExecutionException, InterruptedException, TimeoutException {
        List<Supplier<Long>> tasks = List.of(
                () -> { try { Thread.sleep(200); } catch (InterruptedException e) { Thread.currentThread().interrupt(); } return 1L; },
                () -> 42L,  // моментально
                () -> { try { Thread.sleep(200); } catch (InterruptedException e) { Thread.currentThread().interrupt(); } return 3L; }
        );

        Object result = examples.firstResult(tasks).get(2, TimeUnit.SECONDS);
        assertEquals(42L, result);
    }

    // ── 5. Обработка ошибок ───────────────────────

    @Test
    void safeAsyncCall_returnsSuccessWhenNoFailure() throws ExecutionException, InterruptedException {
        String result = examples.safeAsyncCall(false).get();
        assertEquals("Успех", result);
    }

    @Test
    void safeAsyncCall_returnsFallbackOnException() throws ExecutionException, InterruptedException {
        String result = examples.safeAsyncCall(true).get();
        assertTrue(result.startsWith("Fallback:"));
        assertTrue(result.contains("Что-то пошло не так"));
    }

    @Test
    void safeAsyncCall_doesNotThrowOnFailure() {
        assertDoesNotThrow(() -> examples.safeAsyncCall(true).get());
    }

    // ── 6. Таймаут ────────────────────────────────

    @Test
    void withTimeout_completesWhenFastEnough() throws ExecutionException, InterruptedException {
        Long result = examples.withTimeout(100, 5000).get();
        assertEquals(5050L, result);
    }

    @Test
    void withTimeout_throwsWhenExceeded() {
        // Задача на 10 млн итераций с таймаутом 1мс — должна не успеть
        CompletableFuture<Long> future = examples.withTimeout(10_000_000, 1);

        ExecutionException ex = assertThrows(ExecutionException.class, () -> future.get());
        assertInstanceOf(TimeoutException.class, ex.getCause());
    }

    // ── 7. thenCompose — последовательный pipeline ─

    @Test
    void twoStepPipeline_appliesBothSteps() throws ExecutionException, InterruptedException {
        // simulateHeavyWork(10) = 55, postProcess(55) = "Обработано: 110"
        String result = examples.twoStepPipeline(10).get();
        assertEquals("Обработано: 110", result);
    }

    @Test
    void twoStepPipeline_secondStepRunsAfterFirst() throws ExecutionException, InterruptedException {
        String result = examples.twoStepPipeline(4).get();
        // sum(4) = 10, postProcess = 10 * 2 = 20
        assertEquals("Обработано: 20", result);
    }
}
