package com.highload.highload_presentation.synchronization;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Livelock — активная блокировка.
 *
 * В отличие от deadlock, потоки не заморожены — они активно работают,
 * но бесконечно реагируют друг на друга и не продвигаются вперёд.
 *
 * Аналогия: два человека в узком коридоре уступают дорогу одновременно
 * и вечно перекрывают друг другу путь.
 */
public class LivelockExamples {

    // ─────────────────────────────────────────────
    // ❌ Livelock: два потока вечно «уступают» друг другу
    //
    // Каждый поток видит, что другой активен, и отступает.
    // Но другой тоже видит — и тоже отступает. Цикл повторяется бесконечно.
    // ─────────────────────────────────────────────

    static class PolitePerson {
        private final String name;
        private final AtomicBoolean isActing = new AtomicBoolean(false);

        PolitePerson(String name) { this.name = name; }

        void setActing(boolean acting) { isActing.set(acting); }
        boolean isActing() { return isActing.get(); }
        String getName() { return name; }
    }

    public int demonstrateLivelock(int maxAttempts) {
        PolitePerson alice = new PolitePerson("Alice");
        PolitePerson bob   = new PolitePerson("Bob");

        // Оба флага выставляем ДО старта потоков — гарантируем что оба «активны» с самого начала
        alice.setActing(true);
        bob.setActing(true);

        AtomicInteger aliceAttempts = new AtomicInteger(0);
        AtomicBoolean aliceDone = new AtomicBoolean(false);
        AtomicBoolean bobDone   = new AtomicBoolean(false);

        Thread aliceThread = Thread.ofPlatform().start(() -> {
            while (!aliceDone.get() && aliceAttempts.get() < maxAttempts) {
                if (bob.isActing()) {
                    // ❌ Bob активен — уступаем, отходим назад
                    alice.setActing(false);
                    try { Thread.sleep(1); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                    alice.setActing(true);
                    aliceAttempts.incrementAndGet();
                } else {
                    aliceDone.set(true);
                }
            }
        });

        Thread.ofPlatform().start(() -> {
            while (!bobDone.get() && aliceAttempts.get() < maxAttempts) {
                if (alice.isActing()) {
                    // ❌ Alice активна — уступаем, отходим назад
                    bob.setActing(false);
                    try { Thread.sleep(1); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                    bob.setActing(true);
                } else {
                    bobDone.set(true);
                }
            }
        });

        try { aliceThread.join(2000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

        // Возвращаем количество попыток — при livelock будет равно maxAttempts
        return aliceAttempts.get();
    }

    // ─────────────────────────────────────────────
    // ✅ Решение: случайная задержка перед повторной попыткой (jitter)
    //
    // Потоки ждут разное случайное время — вероятность одновременного
    // столкновения резко снижается. Так работает Ethernet CSMA/CD.
    // ─────────────────────────────────────────────

    public boolean resolveWithJitter() throws InterruptedException {
        AtomicBoolean resource = new AtomicBoolean(false); // false = свободен
        AtomicBoolean done = new AtomicBoolean(false);

        Thread t1 = Thread.ofPlatform().start(() -> {
            while (!done.get()) {
                if (resource.compareAndSet(false, true)) { // ✅ атомарный захват
                    try {
                        done.set(true); // работаем с ресурсом
                    } finally {
                        resource.set(false);
                    }
                } else {
                    try {
                        Thread.sleep((long) (Math.random() * 10)); // ✅ jitter
                    } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                }
            }
        });

        t1.join(500);
        return done.get();
    }
}
