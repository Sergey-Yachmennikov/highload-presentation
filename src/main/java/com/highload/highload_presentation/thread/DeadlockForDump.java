package com.highload.highload_presentation.thread;

import java.util.concurrent.CountDownLatch;

/**
 * Пример deadlock для диагностики через thread dump.
 *
 * Как снять thread dump:
 * 1. jps — найти PID
 * 2. jstack <PID> — снять dump
 *
 * В выводе будет:
 * "Found one Java-level deadlock:"
 * с указанием какие потоки и какие мониторы участвуют.
 */
public class DeadlockForDump {

    private final Object resourceA = new Object();
    private final Object resourceB = new Object();

    /**
     * Запускает deadlock и НЕ завершается — висит до kill.
     * Предназначен для ручной диагностики через jstack/VisualVM.
     */
    public void startDeadlock() throws InterruptedException {
        CountDownLatch bothLocked = new CountDownLatch(2);

        Thread t1 = Thread.ofPlatform().name("worker-A-then-B").start(() -> {
            synchronized (resourceA) {
                System.out.println(Thread.currentThread().getName() + " захватил resourceA");
                bothLocked.countDown();
                awaitQuietly(bothLocked);
                System.out.println(Thread.currentThread().getName() + " пытается захватить resourceB...");
                synchronized (resourceB) {
                    System.out.println("Сюда мы никогда не попадём");
                }
            }
        });

        Thread t2 = Thread.ofPlatform().name("worker-B-then-A").start(() -> {
            synchronized (resourceB) {
                System.out.println(Thread.currentThread().getName() + " захватил resourceB");
                bothLocked.countDown();
                awaitQuietly(bothLocked);
                System.out.println(Thread.currentThread().getName() + " пытается захватить resourceA...");
                synchronized (resourceA) {
                    System.out.println("Сюда мы никогда не попадём");
                }
            }
        });

        t1.join();
        t2.join();
    }

    private void awaitQuietly(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Для запуска из терминала:
     * mvn exec:java -Dexec.mainClass="com.highload.highload_presentation.thread.DeadlockForDump"
     */
    public static void main(String[] args) throws InterruptedException {
        System.out.println("PID: " + ProcessHandle.current().pid());
        System.out.println("Запускаем deadlock... Снимите thread dump: jstack " + ProcessHandle.current().pid());
        new DeadlockForDump().startDeadlock();
    }
}
