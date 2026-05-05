package com.highload.highload_presentation.synchronization;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Deadlock — взаимная блокировка.
 *
 * Поток A держит ресурс 1 и ждёт ресурс 2.
 * Поток B держит ресурс 2 и ждёт ресурс 1.
 * Оба ждут вечно — программа зависает.
 *
 * Условия возникновения (условия Коффмана):
 *  1. Взаимное исключение — ресурс занят только одним потоком
 *  2. Удержание и ожидание — поток держит ресурс и ждёт другой
 *  3. Нет принудительного освобождения — ресурс освобождается добровольно
 *  4. Циклическое ожидание — цепочка потоков ждёт друг друга
 */
public class DeadlockExamples {

    private final Object lockA = new Object();
    private final Object lockB = new Object();

    // ─────────────────────────────────────────────
    // ❌ Deadlock: потоки захватывают блокировки в разном порядке
    // ─────────────────────────────────────────────

    public Thread startDeadlockThreadA() {
        return Thread.ofPlatform().start(() -> {
            synchronized (lockA) {
                System.out.println("Thread A: держит lockA, ждёт lockB");
                try { Thread.sleep(50); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                synchronized (lockB) { // ❌ ждёт lockB, который держит Thread B
                    System.out.println("Thread A: захватил оба лока");
                }
            }
        });
    }

    public Thread startDeadlockThreadB() {
        return Thread.ofPlatform().start(() -> {
            synchronized (lockB) {
                System.out.println("Thread B: держит lockB, ждёт lockA");
                try { Thread.sleep(50); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                synchronized (lockA) { // ❌ ждёт lockA, который держит Thread A
                    System.out.println("Thread B: захватил оба лока");
                }
            }
        });
    }

    // ─────────────────────────────────────────────
    // ✅ Решение 1: единый порядок захвата блокировок
    // Оба потока захватывают lockA → lockB. Никогда не возникнет цикл.
    // ─────────────────────────────────────────────

    public Thread startSafeThreadA() {
        return Thread.ofPlatform().start(() -> {
            synchronized (lockA) {           // ✅ всегда сначала lockA
                try { Thread.sleep(50); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                synchronized (lockB) {       // ✅ потом lockB
                    System.out.println("Safe Thread A: выполнился");
                }
            }
        });
    }

    public Thread startSafeThreadB() {
        return Thread.ofPlatform().start(() -> {
            synchronized (lockA) {           // ✅ тот же порядок — lockA
                try { Thread.sleep(50); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                synchronized (lockB) {       // ✅ потом lockB
                    System.out.println("Safe Thread B: выполнился");
                }
            }
        });
    }

    // ─────────────────────────────────────────────
    // ✅ Решение 2: tryLock с таймаутом
    // Поток пробует захватить блокировку. Если не удаётся — отступает и пробует снова.
    // ─────────────────────────────────────────────

    private final ReentrantLock reentrantA = new ReentrantLock();
    private final ReentrantLock reentrantB = new ReentrantLock();

    public boolean tryLockBothOrBackoff() throws InterruptedException {
        while (true) {
            boolean gotA = reentrantA.tryLock(50, TimeUnit.MILLISECONDS);
            if (!gotA) continue;
            try {
                boolean gotB = reentrantB.tryLock(50, TimeUnit.MILLISECONDS);
                if (!gotB) continue; // ✅ отступаем, освобождаем A и пробуем снова
                try {
                    // Работаем с обоими ресурсами
                    return true;
                } finally {
                    reentrantB.unlock();
                }
            } finally {
                reentrantA.unlock();
            }
        }
    }
}
