package com.highload.highload_presentation.async;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Примеры асинхронности через Virtual Threads (Project Loom, Java 21+).
 *
 * Виртуальные потоки — лёгкие потоки, управляемые JVM, а не ОС.
 * Можно создать миллионы без нехватки ресурсов.
 *
 * Главное отличие от обычных потоков:
 *  - Platform thread (обычный) — 1:1 с потоком ОС, ~1MB стека, дорогой
 *  - Virtual thread — M:N с потоками ОС, ~кБ стека, дешёвый
 *
 * Когда виртуальный поток блокируется (sleep, I/O, lock) —
 * JVM снимает его с carrier thread и ставит другой. Carrier thread не простаивает.
 */
public class VirtualThreadExamples {

    // ─────────────────────────────────────────────
    // 1. Создание одного виртуального потока
    //
    // Thread.ofVirtual() — фабрика виртуальных потоков.
    // API идентичен обычным потокам.
    // ─────────────────────────────────────────────

    public String runInVirtualThread(String task) throws InterruptedException {
        List<String> result = new ArrayList<>();

        Thread vThread = Thread.ofVirtual()
                .name("vt-worker")
                .start(() -> result.add("Выполнено: " + task));

        vThread.join();
        return result.get(0);
    }

    // ─────────────────────────────────────────────
    // 2. ExecutorService с виртуальными потоками
    //
    // newVirtualThreadPerTaskExecutor() — каждая задача получает
    // свой виртуальный поток. Пул не нужен — потоки дешёвые.
    // ─────────────────────────────────────────────

    public List<String> runManyTasksWithVirtualThreads(List<String> tasks)
            throws InterruptedException, ExecutionException {
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<String>> futures = tasks.stream()
                    .map(task -> executor.submit(() -> simulateIoWork(task)))
                    .toList();

            List<String> results = new ArrayList<>();
            for (Future<String> f : futures) {
                results.add(f.get());
            }
            return results;
        }
    }

    // ─────────────────────────────────────────────
    // 3. Виртуальные потоки vs Platform потоки при I/O
    //
    // При блокирующем I/O platform thread простаивает и держит ресурс ОС.
    // Virtual thread освобождает carrier thread и не тратит ресурсы.
    //
    // Демонстрация: N задач с имитацией I/O-задержки.
    // Виртуальные потоки завершают N задач почти так же быстро, как 1.
    // ─────────────────────────────────────────────

    public long measureVirtualThreads(int taskCount, long sleepMs)
            throws InterruptedException, ExecutionException {
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            long start = System.currentTimeMillis();

            List<Future<?>> futures = new ArrayList<>();
            for (int i = 0; i < taskCount; i++) {
                futures.add(executor.submit(() -> {
                    Thread.sleep(sleepMs); // имитация блокирующего I/O
                    return null;
                }));
            }
            for (Future<?> f : futures) f.get();

            return System.currentTimeMillis() - start;
        }
    }

    public long measurePlatformThreads(int taskCount, long sleepMs)
            throws InterruptedException, ExecutionException {
        // Фиксированный пул — platform threads ограничены числом ядер
        try (ExecutorService executor = Executors.newFixedThreadPool(
                Runtime.getRuntime().availableProcessors())) {
            long start = System.currentTimeMillis();

            List<Future<?>> futures = new ArrayList<>();
            for (int i = 0; i < taskCount; i++) {
                futures.add(executor.submit(() -> {
                    Thread.sleep(sleepMs);
                    return null;
                }));
            }
            for (Future<?> f : futures) f.get();

            return System.currentTimeMillis() - start;
        }
    }

    // ─────────────────────────────────────────────
    // 4. Проверка типа потока
    //
    // Thread.isVirtual() — отличаем виртуальный поток от платформенного.
    // ─────────────────────────────────────────────

    public boolean isRunningInVirtualThread() throws InterruptedException {
        boolean[] result = new boolean[1];
        Thread vThread = Thread.ofVirtual().start(
                () -> result[0] = Thread.currentThread().isVirtual()
        );
        vThread.join();
        return result[0];
    }

    public boolean isRunningInPlatformThread() throws InterruptedException {
        boolean[] result = new boolean[1];
        Thread pThread = Thread.ofPlatform().start(
                () -> result[0] = Thread.currentThread().isVirtual()
        );
        pThread.join();
        return result[0];
    }

    // ── Вспомогательный метод ─────────────────────

    private String simulateIoWork(String input) throws InterruptedException {
        Thread.sleep(10); // имитация I/O (БД, сеть)
        return "done:" + input;
    }
}
