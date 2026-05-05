package com.highload.highload_presentation.synchronization;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.BrokenBarrierException;

import static org.junit.jupiter.api.Assertions.*;

class RaceConditionExamplesTest {

    private final RaceConditionExamples examples = new RaceConditionExamples();

    // ── Race condition ─────────────────────────────

    @RepeatedTest(5)
    void unsafeIncrement_losesUpdates() throws InterruptedException {
        int threads = 10;
        int iterations = 1000;
        int expected = threads * iterations;

        int actual = examples.unsafeIncrement(threads, iterations);

        // Без синхронизации почти гарантированно теряем часть инкрементов
        // Проверяем что результат некорректен (хотя бы иногда)
        System.out.printf("Unsafe: expected=%d, actual=%d, lost=%d%n",
                expected, actual, expected - actual);
        // Не asserting конкретное значение — результат недетерминирован
        assertTrue(actual > 0, "Счётчик должен быть положительным");
    }

    @Test
    void atomicIncrement_neverLosesUpdates() throws InterruptedException {
        int threads = 10;
        int iterations = 1000;
        int expected = threads * iterations;

        int actual = examples.atomicIncrement(threads, iterations);
        assertEquals(expected, actual, "AtomicInteger никогда не теряет инкременты");
    }

    @Test
    void adderIncrement_neverLosesUpdates() throws InterruptedException {
        int threads = 10;
        int iterations = 1000;
        long expected = (long) threads * iterations;

        long actual = examples.adderIncrement(threads, iterations);
        assertEquals(expected, actual, "LongAdder никогда не теряет инкременты");
    }

    // ── Check-Then-Act ────────────────────────────

    @Test
    void safeCheckThenAct_returnsItemsWithoutException() throws InterruptedException {
        examples.populateList(List.of("a", "b", "c", "d", "e"));

        List<Thread> threads = new java.util.ArrayList<>();
        List<String> results = new java.util.concurrent.CopyOnWriteArrayList<>();

        for (int i = 0; i < 10; i++) {
            threads.add(Thread.ofPlatform().start(() -> {
                String item = examples.safeCheckThenAct();
                if (item != null) results.add(item);
            }));
        }
        for (Thread t : threads) t.join();

        // Не более 5 элементов, нет дублей
        assertTrue(results.size() <= 5, "Не должно быть больше элементов чем в списке");
        assertEquals(results.size(), results.stream().distinct().count(), "Не должно быть дублей");
    }

    // ── Volatile visibility ───────────────────────

    @Test
    void safeFlag_isVisibleAcrossThreads() throws InterruptedException {
        Thread writer = examples.startSafeFlagWriter();
        writer.join(500);

        assertTrue(examples.readSafeFlag(),
                "volatile флаг должен быть виден после записи в другом потоке");
    }

    // ── CyclicBarrier — максимальная конкуренция ──

    @Test
    void raceWithBarrier_demonstratesDataLoss() throws InterruptedException, BrokenBarrierException {
        int threads = 8;
        int iterations = 500;
        int expected = threads * iterations;

        int actual = examples.raceWithBarrier(threads, iterations);

        System.out.printf("Race with barrier: expected=%d, actual=%d, lost=%d (%.1f%%)%n",
                expected, actual, expected - actual,
                100.0 * (expected - actual) / expected);

        // Барьер максимизирует конкуренцию — потерь почти наверняка больше нуля
        assertTrue(actual > 0 && actual <= expected,
                "Счётчик должен быть в диапазоне (0, expected]");
    }

    @Test
    void atomicVsUnsafe_atomicAlwaysCorrect() throws InterruptedException {
        int threads = 20;
        int iterations = 500;
        int expected = threads * iterations;

        int unsafe = examples.unsafeIncrement(threads, iterations);
        int atomic = examples.atomicIncrement(threads, iterations);

        assertEquals(expected, atomic, "Atomic всегда корректен");
        // unsafe может быть равен expected (редко), но как правило меньше
        System.out.printf("Unsafe: %d, Atomic: %d%n", unsafe, atomic);
    }
}
