package com.highload.highload_presentation.synchronization;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;

/**
 * Race Condition — состояние гонки.
 *
 * Результат зависит от порядка выполнения потоков.
 * Операция read-modify-write не атомарна: между чтением и записью
 * другой поток может изменить значение.
 *
 * Check-Then-Act — ещё одна форма гонки:
 * между проверкой условия и действием состояние успевает измениться.
 */
public class RaceConditionExamples {

    // ─────────────────────────────────────────────
    // ❌ Race condition: i++ не атомарна
    // i++ == read(i) → i+1 → write(i) — три отдельные операции.
    // Два потока могут прочитать одно значение и оба записать одинаковый результат.
    // ─────────────────────────────────────────────

    private int unsafeCounter = 0;

    public int unsafeIncrement(int threads, int iterationsPerThread) throws InterruptedException {
        unsafeCounter = 0;
        List<Thread> list = new ArrayList<>();
        for (int t = 0; t < threads; t++) {
            list.add(Thread.ofPlatform().start(() -> {
                for (int i = 0; i < iterationsPerThread; i++) {
                    unsafeCounter++; // ❌ не атомарно
                }
            }));
        }
        for (Thread thread : list) thread.join();
        return unsafeCounter;
    }

    // ─────────────────────────────────────────────
    // ✅ Решение 1: AtomicInteger — lock-free атомарные операции
    // ─────────────────────────────────────────────

    private final AtomicInteger atomicCounter = new AtomicInteger(0);

    public int atomicIncrement(int threads, int iterationsPerThread) throws InterruptedException {
        atomicCounter.set(0);
        List<Thread> list = new ArrayList<>();
        for (int t = 0; t < threads; t++) {
            list.add(Thread.ofPlatform().start(() -> {
                for (int i = 0; i < iterationsPerThread; i++) {
                    atomicCounter.incrementAndGet(); // ✅ атомарная CAS-операция
                }
            }));
        }
        for (Thread thread : list) thread.join();
        return atomicCounter.get();
    }

    // ─────────────────────────────────────────────
    // ✅ Решение 2: LongAdder — лучше AtomicLong при высокой конкуренции
    // Разбивает счётчик на ячейки per-thread, суммирует при чтении.
    // ─────────────────────────────────────────────

    private final LongAdder adderCounter = new LongAdder();

    public long adderIncrement(int threads, int iterationsPerThread) throws InterruptedException {
        adderCounter.reset();
        List<Thread> list = new ArrayList<>();
        for (int t = 0; t < threads; t++) {
            list.add(Thread.ofPlatform().start(() -> {
                for (int i = 0; i < iterationsPerThread; i++) {
                    adderCounter.increment(); // ✅ минимальная конкуренция между потоками
                }
            }));
        }
        for (Thread thread : list) thread.join();
        return adderCounter.sum();
    }

    // ─────────────────────────────────────────────
    // ❌ Check-Then-Act: проверка и действие не атомарны
    //
    // Поток A: проверяет isEmpty() → false → засыпает
    // Поток B: забирает последний элемент
    // Поток A: просыпается, вызывает remove() → IndexOutOfBoundsException
    // ─────────────────────────────────────────────

    private final List<String> sharedList = new ArrayList<>();

    public void populateList(List<String> items) {
        sharedList.clear();
        sharedList.addAll(items);
    }

    public String unsafeCheckThenAct() {
        if (!sharedList.isEmpty()) {    // ❌ проверка
            return sharedList.remove(0); // ❌ действие — между ними другой поток мог изменить список
        }
        return null;
    }

    public synchronized String safeCheckThenAct() { // ✅ атомарная проверка + действие
        if (!sharedList.isEmpty()) {
            return sharedList.remove(0);
        }
        return null;
    }

    // ─────────────────────────────────────────────
    // ❌ Visibility problem: изменения в одном потоке не видны другому
    //
    // JVM может кэшировать переменную в регистре процессора.
    // Без volatile второй поток может никогда не увидеть изменение.
    // ─────────────────────────────────────────────

    private boolean unsafeFlag = false;
    private volatile boolean safeFlag = false; // ✅ volatile гарантирует видимость

    public Thread startUnsafeFlagWriter() {
        return Thread.ofPlatform().start(() -> {
            try { Thread.sleep(10); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            unsafeFlag = true; // ❌ может быть не видно другому потоку
        });
    }

    public Thread startSafeFlagWriter() {
        return Thread.ofPlatform().start(() -> {
            try { Thread.sleep(10); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            safeFlag = true; // ✅ volatile — запись видна всем потокам немедленно
        });
    }

    public boolean readSafeFlag() { return safeFlag; }

    // ─────────────────────────────────────────────
    // Демонстрация: CyclicBarrier — старт всех потоков одновременно
    // Максимизирует конкуренцию для воспроизведения гонки.
    // ─────────────────────────────────────────────

    public int raceWithBarrier(int threads, int iterations) throws InterruptedException, BrokenBarrierException {
        unsafeCounter = 0;
        CyclicBarrier barrier = new CyclicBarrier(threads);
        List<Thread> list = new ArrayList<>();

        for (int t = 0; t < threads; t++) {
            list.add(Thread.ofPlatform().start(() -> {
                try {
                    barrier.await(); // ✅ все потоки стартуют строго одновременно
                } catch (InterruptedException | BrokenBarrierException e) {
                    Thread.currentThread().interrupt();
                }
                for (int i = 0; i < iterations; i++) {
                    unsafeCounter++; // ❌ максимальная конкуренция → максимум потерь
                }
            }));
        }
        for (Thread thread : list) thread.join();
        return unsafeCounter;
    }
}
