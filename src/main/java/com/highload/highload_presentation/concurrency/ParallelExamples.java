package com.highload.highload_presentation.concurrency;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.LongStream;

/**
 * Примеры задач, которые МОЖНО распараллелить.
 *
 * Задача подходит для параллелизации если:
 *  - Итерации независимы друг от друга (нет зависимости по данным)
 *  - Задача легко делится на одинаковые подзадачи
 *  - Вычисления достаточно "тяжёлые", чтобы оправдать накладные расходы на потоки
 */
public class ParallelExamples {

    // ─────────────────────────────────────────────
    // ✅ Суммирование большого массива
    // Каждый поток считает сумму своего сегмента — результаты складываются.
    // Паттерн: Divide and Conquer / Fork-Join
    // ─────────────────────────────────────────────

    public long parallelSum(long[] array) throws InterruptedException, ExecutionException {
        int cores = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(cores);

        int chunkSize = array.length / cores;
        List<Future<Long>> futures = new ArrayList<>();

        for (int i = 0; i < cores; i++) {
            int from = i * chunkSize;
            int to = (i == cores - 1) ? array.length : from + chunkSize;

            futures.add(executor.submit(() -> {
                long sum = 0;
                for (int j = from; j < to; j++) sum += array[j];
                return sum;
            }));
        }

        long total = 0;
        for (Future<Long> f : futures) total += f.get();
        executor.shutdown();
        return total;
    }

    // Тот же результат через parallel stream — синтаксически проще:
    public long parallelSumStream(long[] array) {
        return Arrays.stream(array).parallel().sum();
    }

    // ─────────────────────────────────────────────
    // ✅ Обработка независимых элементов коллекции
    // Каждый элемент обрабатывается независимо — идеально для параллелизации.
    // Паттерн: Map (трансформация без состояния)
    // ─────────────────────────────────────────────

    public List<String> processItemsInParallel(List<String> items) {
        return items.parallelStream()
                .map(item -> heavyTransform(item))
                .toList();
    }

    private String heavyTransform(String input) {
        // Имитация тяжёлой CPU-работы над одним элементом
        return input.toUpperCase().repeat(2);
    }

    // ─────────────────────────────────────────────
    // ✅ Независимые HTTP-запросы / I/O задачи
    // Потоки не ждут друг друга — каждый делает свой запрос.
    // Паттерн: Fan-out (разветвление)
    // ─────────────────────────────────────────────

    public List<String> fetchAllInParallel(List<String> urls) throws InterruptedException {
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor(); // Java 21+
        List<Future<String>> futures = urls.stream()
                .map(url -> executor.submit(() -> simulateFetch(url)))
                .toList();

        List<String> results = new ArrayList<>();
        for (Future<String> f : futures) {
            try {
                results.add(f.get(5, TimeUnit.SECONDS));
            } catch (ExecutionException | TimeoutException e) {
                results.add("ERROR: " + e.getMessage());
            }
        }
        executor.shutdown();
        return results;
    }

    private String simulateFetch(String url) throws InterruptedException {
        Thread.sleep(100); // имитация сетевой задержки
        return "Response from: " + url;
    }

    // ─────────────────────────────────────────────
    // ✅ Fork-Join: рекурсивное разбиение (Fibonacci через ForkJoin)
    // ForkJoinPool эффективен для рекурсивных задач типа "разделяй и властвуй".
    // ─────────────────────────────────────────────

    public long forkJoinFibonacci(int n) {
        return ForkJoinPool.commonPool().invoke(new FibTask(n));
    }

    static class FibTask extends RecursiveTask<Long> {
        private static final int THRESHOLD = 10; // ниже порога — считаем последовательно
        private final int n;

        FibTask(int n) { this.n = n; }

        @Override
        protected Long compute() {
            if (n <= THRESHOLD) return computeSequentially(n);
            FibTask f1 = new FibTask(n - 1);
            FibTask f2 = new FibTask(n - 2);
            f1.fork();
            return f2.compute() + f1.join();
        }

        private long computeSequentially(int n) {
            if (n <= 1) return n;
            return computeSequentially(n - 1) + computeSequentially(n - 2);
        }
    }
}
