package com.highload.highload_presentation.synchronization;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Thread Starvation — голодание потоков.
 *
 * Поток готов к выполнению, но планировщик постоянно выбирает другие потоки.
 * Низкоприоритетный поток не получает процессорного времени — «голодает».
 *
 * Причины:
 *  - Несправедливые блокировки (нет очереди — всегда выигрывает "быстрый")
 *  - Приоритеты потоков (высокоприоритетные вытесняют низкоприоритетные)
 *  - Долгие synchronized-блоки, которые не дают войти другим
 */
public class StarvationExamples {

    // ─────────────────────────────────────────────
    // ❌ Starvation через нечестный (unfair) ReentrantLock
    //
    // По умолчанию ReentrantLock — нечестный: поток, только что освободивший
    // блокировку, может тут же захватить её снова. Другие потоки ждут бесконечно.
    // ─────────────────────────────────────────────

    private final ReentrantLock unfairLock = new ReentrantLock(false); // ❌ unfair

    public int[] demonstrateStarvation(int durationMs) throws InterruptedException {
        AtomicInteger highPriorityCount = new AtomicInteger(0);
        AtomicInteger lowPriorityCount  = new AtomicInteger(0);
        AtomicInteger running = new AtomicInteger(1);

        // Высокоприоритетный поток — захватывает лок в тугом цикле
        Thread highPriority = Thread.ofPlatform().start(() -> {
            while (running.get() == 1) {
                unfairLock.lock();
                try {
                    highPriorityCount.incrementAndGet();
                } finally {
                    unfairLock.unlock();
                }
            }
        });

        // Низкоприоритетный поток — почти никогда не получает лок
        Thread lowPriority = Thread.ofPlatform().start(() -> {
            while (running.get() == 1) {
                unfairLock.lock(); // ❌ почти никогда не получает лок
                try {
                    lowPriorityCount.incrementAndGet();
                } finally {
                    unfairLock.unlock();
                }
            }
        });

        Thread.sleep(durationMs);
        running.set(0);
        highPriority.join(500);
        lowPriority.join(500);

        return new int[]{highPriorityCount.get(), lowPriorityCount.get()};
    }

    // ─────────────────────────────────────────────
    // ✅ Решение: честный (fair) ReentrantLock
    //
    // fair=true гарантирует FIFO-очередь: поток, ждущий дольше, получает лок первым.
    // Никто не голодает — но пропускная способность немного снижается.
    // ─────────────────────────────────────────────

    private final ReentrantLock fairLock = new ReentrantLock(true); // ✅ fair

    public int[] demonstrateFairness(int durationMs) throws InterruptedException {
        AtomicInteger count1 = new AtomicInteger(0);
        AtomicInteger count2 = new AtomicInteger(0);
        AtomicInteger running = new AtomicInteger(1);

        Thread t1 = Thread.ofPlatform().start(() -> {
            while (running.get() == 1) {
                fairLock.lock();
                try { count1.incrementAndGet(); }
                finally { fairLock.unlock(); }
            }
        });

        Thread t2 = Thread.ofPlatform().start(() -> {
            while (running.get() == 1) {
                fairLock.lock(); // ✅ честная очередь — оба потока получают доступ
                try { count2.incrementAndGet(); }
                finally { fairLock.unlock(); }
            }
        });

        Thread.sleep(durationMs);
        running.set(0);
        t1.join(500);
        t2.join(500);

        return new int[]{count1.get(), count2.get()};
    }

    // ─────────────────────────────────────────────
    // ❌ Starvation через приоритеты потоков
    //
    // MAX_PRIORITY потоки постоянно вытесняют MIN_PRIORITY.
    // На реальных системах эффект зависит от ОС.
    // ─────────────────────────────────────────────

    public int[] demonstratePriorityStarvation(int durationMs) throws InterruptedException {
        AtomicInteger highCount = new AtomicInteger(0);
        AtomicInteger lowCount  = new AtomicInteger(0);
        AtomicInteger running = new AtomicInteger(1);

        Thread highThread = new Thread(() -> {
            while (running.get() == 1) highCount.incrementAndGet();
        });
        highThread.setPriority(Thread.MAX_PRIORITY); // ❌ максимальный приоритет

        Thread lowThread = new Thread(() -> {
            while (running.get() == 1) lowCount.incrementAndGet();
        });
        lowThread.setPriority(Thread.MIN_PRIORITY); // ❌ минимальный приоритет

        highThread.start();
        lowThread.start();

        Thread.sleep(durationMs);
        running.set(0);
        highThread.join(500);
        lowThread.join(500);

        return new int[]{highCount.get(), lowCount.get()};
    }
}
