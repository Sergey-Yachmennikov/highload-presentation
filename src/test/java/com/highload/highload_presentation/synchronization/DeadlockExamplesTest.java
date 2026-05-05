package com.highload.highload_presentation.synchronization;

import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class DeadlockExamplesTest {

    private final DeadlockExamples examples = new DeadlockExamples();

    // ─────────────────────────────────────────────
    // Safe threads — должны завершиться
    // (Deadlock потоки не тестируем напрямую — они зависнут)
    // ─────────────────────────────────────────────

    @Test
    void safeThreads_bothComplete() throws InterruptedException {
        Thread a = examples.startSafeThreadA();
        Thread b = examples.startSafeThreadB();

        a.join(2000);
        b.join(2000);

        // Оба потока завершились — дедлока нет
        assertFalse(a.isAlive(), "Safe Thread A должен завершиться");
        assertFalse(b.isAlive(), "Safe Thread B должен завершиться");
    }

    @Test
    void tryLockBothOrBackoff_succeedsWithoutDeadlock() throws InterruptedException {
        // Два потока одновременно пытаются захватить оба лока — ни один не должен зависнуть
        boolean[] result = {false, false};
        Thread t1 = Thread.ofPlatform().start(() -> {
            try { result[0] = examples.tryLockBothOrBackoff(); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        });
        Thread t2 = Thread.ofPlatform().start(() -> {
            try { result[1] = examples.tryLockBothOrBackoff(); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        });

        t1.join(3000);
        t2.join(3000);

        assertFalse(t1.isAlive(), "Поток t1 должен завершиться");
        assertFalse(t2.isAlive(), "Поток t2 должен завершиться");
        assertTrue(result[0], "t1 должен успешно захватить оба лока");
        assertTrue(result[1], "t2 должен успешно захватить оба лока");
    }

    @Test
    void deadlockThreads_doNotCompleteWithinTimeout() throws InterruptedException {
        // Демонстрация: deadlock-потоки зависают и не завершаются
        Thread a = examples.startDeadlockThreadA();
        Thread b = examples.startDeadlockThreadB();

        // Ждём 500ms — они не должны завершиться
        a.join(500);
        b.join(500);

        assertTrue(a.isAlive() || b.isAlive(),
                "Хотя бы один поток должен быть заблокирован (deadlock)");

        // Прерываем потоки после теста чтобы не засорять JVM
        a.interrupt();
        b.interrupt();
    }
}
