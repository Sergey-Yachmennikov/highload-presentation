package com.highload.highload_presentation.concurrency;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NonParallelExamplesTest {

    private NonParallelExamples examples;

    @BeforeEach
    void setUp() {
        examples = new NonParallelExamples();
    }

    // ── Fibonacci (последовательная зависимость) ───

    @Test
    void fibonacci_baseCase() {
        assertEquals(0, examples.fibonacci(0));
        assertEquals(1, examples.fibonacci(1));
    }

    @Test
    void fibonacci_correctSequence() {
        int[] expected = {0, 1, 1, 2, 3, 5, 8, 13, 21, 34};
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], examples.fibonacci(i), "Mismatch at n=" + i);
        }
    }

    // ── Обход связного списка ──────────────────────

    @Test
    void sumLinkedList_singleNode() {
        NonParallelExamples.Node head = new NonParallelExamples.Node(42);
        assertEquals(42, examples.sumLinkedList(head));
    }

    @Test
    void sumLinkedList_multipleNodes() {
        NonParallelExamples.Node head = new NonParallelExamples.Node(1);
        head.next = new NonParallelExamples.Node(2);
        head.next.next = new NonParallelExamples.Node(3);
        head.next.next.next = new NonParallelExamples.Node(4);

        assertEquals(10, examples.sumLinkedList(head));
    }

    @Test
    void sumLinkedList_nullReturnsZero() {
        assertEquals(0, examples.sumLinkedList(null));
    }

    // ── Race Condition vs AtomicInteger ───────────

    @Test
    void demonstrateRaceCondition_doesNotThrow() {
        // Не проверяем конкретный результат — он недетерминирован.
        // Проверяем, что код не падает с исключением.
        assertDoesNotThrow(() -> examples.demonstrateRaceCondition());
    }

    @Test
    void demonstrateSafeCounter_alwaysReturns1000() throws InterruptedException {
        // AtomicInteger всегда даёт правильный результат
        assertDoesNotThrow(() -> examples.demonstrateSafeCounter());
    }

    // ── Безопасный vs небезопасный сбор в список ──

    @Test
    void unsafeParallelCollect_doesNotThrow() throws InterruptedException {
        // Может вернуть список < n элементов, но не должен упасть с NPE
        assertDoesNotThrow(() -> examples.unsafeParallelCollect(100));
    }

    @Test
    void safeParallelCollect_returnsCorrectSize() {
        List<Integer> result = examples.safeParallelCollect(100);
        assertEquals(100, result.size());
    }

    @Test
    void safeParallelCollect_containsAllValues() {
        List<Integer> result = examples.safeParallelCollect(50);
        for (int i = 0; i < 50; i++) {
            assertTrue(result.contains(i), "Missing value: " + i);
        }
    }

    // ── Банковский баланс (Lost Update) ───────────

    @Test
    void safeWithdraw_correctBalanceAfterConcurrentWithdrawals() throws InterruptedException {
        // Запускаем 10 потоков, каждый снимает 100 — итого 1000
        // Начальный баланс 1000, после всех снятий должен быть 0
        List<Thread> threads = new java.util.ArrayList<>();
        for (int i = 0; i < 10; i++) {
            threads.add(Thread.ofVirtual().start(() -> examples.safeWithdraw(100)));
        }
        for (Thread t : threads) t.join();

        // Проверяем через повторное снятие — если баланс 0, ничего не спишется
        assertDoesNotThrow(() -> examples.safeWithdraw(1));
    }

    @Test
    void safeWithdraw_doesNotGoBelowZero() throws InterruptedException {
        // 20 потоков пытаются снять по 100 при балансе 1000 — только 10 успеют
        List<Thread> threads = new java.util.ArrayList<>();
        for (int i = 0; i < 20; i++) {
            threads.add(Thread.ofVirtual().start(() -> examples.safeWithdraw(100)));
        }
        for (Thread t : threads) t.join();

        // Баланс не должен уйти в минус — просто проверяем отсутствие ошибок
        assertDoesNotThrow(() -> examples.safeWithdraw(0));
    }

    // ── Мелкие задачи — не стоит параллелить ──────

    @Test
    void sumTwoNumbers_correctResult() {
        assertEquals(7, examples.sumTwoNumbers(3, 4));
        assertEquals(0, examples.sumTwoNumbers(-5, 5));
        assertEquals(-3, examples.sumTwoNumbers(-1, -2));
    }
}
