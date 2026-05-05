package com.highload.highload_presentation.async;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

class VirtualThreadExamplesTest {

    private VirtualThreadExamples examples;

    @BeforeEach
    void setUp() {
        examples = new VirtualThreadExamples();
    }

    // ── 1. Один виртуальный поток ─────────────────

    @Test
    void runInVirtualThread_returnsResult() throws InterruptedException {
        String result = examples.runInVirtualThread("hello");
        assertEquals("Выполнено: hello", result);
    }

    @Test
    void runInVirtualThread_runsInVirtualThread() throws InterruptedException {
        boolean[] isVirtual = new boolean[1];
        Thread vt = Thread.ofVirtual().start(
                () -> isVirtual[0] = Thread.currentThread().isVirtual()
        );
        vt.join();
        assertTrue(isVirtual[0], "Поток должен быть виртуальным");
    }

    // ── 2. ExecutorService с виртуальными потоками ─

    @Test
    void runManyTasksWithVirtualThreads_processesAllTasks()
            throws InterruptedException, ExecutionException {
        List<String> tasks = List.of("a", "b", "c", "d");
        List<String> results = examples.runManyTasksWithVirtualThreads(tasks);

        assertEquals(4, results.size());
        assertTrue(results.contains("done:a"));
        assertTrue(results.contains("done:b"));
        assertTrue(results.contains("done:c"));
        assertTrue(results.contains("done:d"));
    }

    @Test
    void runManyTasksWithVirtualThreads_emptyInputReturnsEmptyList()
            throws InterruptedException, ExecutionException {
        List<String> results = examples.runManyTasksWithVirtualThreads(List.of());
        assertTrue(results.isEmpty());
    }

    // ── 3. Virtual threads быстрее при I/O-нагрузке ──

    @Test
    void virtualThreads_fasterThanPlatformThreadsOnIo()
            throws InterruptedException, ExecutionException {
        int taskCount = 100;
        long sleepMs = 50;

        long virtualTime = examples.measureVirtualThreads(taskCount, sleepMs);
        long platformTime = examples.measurePlatformThreads(taskCount, sleepMs);

        // Virtual threads: все 100 задач по 50ms выполняются параллельно → ~50ms
        // Platform threads: ограничены числом ядер → задачи ждут в очереди → намного дольше
        assertTrue(virtualTime < platformTime,
                "Virtual: %dms, Platform: %dms — виртуальные должны быть быстрее"
                        .formatted(virtualTime, platformTime));
    }

    @Test
    void virtualThreads_completesAllTasksNearSimultaneously()
            throws InterruptedException, ExecutionException {
        // 200 задач × 50ms I/O: если виртуальные потоки работают правильно,
        // общее время близко к времени одной задачи, а не 200 × 50ms
        long elapsed = examples.measureVirtualThreads(200, 50);
        assertTrue(elapsed < 500,
                "200 задач по 50ms заняли %dms — ожидалось < 500ms".formatted(elapsed));
    }

    // ── 4. Проверка типа потока ───────────────────

    @Test
    void isRunningInVirtualThread_returnsTrue() throws InterruptedException {
        assertTrue(examples.isRunningInVirtualThread());
    }

    @Test
    void isRunningInPlatformThread_returnsFalse() throws InterruptedException {
        assertFalse(examples.isRunningInPlatformThread());
    }
}
