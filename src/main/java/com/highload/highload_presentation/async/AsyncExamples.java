package com.highload.highload_presentation.async;

import java.util.List;
import java.util.concurrent.*;
import java.util.function.Supplier;

/**
 * Примеры вынесения тяжёлых вычислений в отдельный поток.
 *
 * Основная идея: не блокировать главный поток — отдать задачу в пул,
 * а результат забрать позже (или обработать в callback).
 */
public class AsyncExamples {

    private final ExecutorService executor;

    public AsyncExamples(ExecutorService executor) {
        this.executor = executor;
    }

    // ─────────────────────────────────────────────
    // 1. Future — «пообещай результат»
    //
    // Отправляем задачу в пул, получаем Future.
    // Главный поток продолжает работу, результат забирает через future.get().
    // Минус: future.get() блокирует поток до готовности результата.
    // ─────────────────────────────────────────────

    public Future<Long> heavyComputationAsync(int n) {
        return executor.submit(() -> simulateHeavyWork(n));
    }

    // ─────────────────────────────────────────────
    // 2. CompletableFuture — неблокирующая цепочка
    //
    // Позволяет строить pipeline: вычислить → трансформировать → обработать.
    // Главный поток не ждёт — всё происходит в пуле.
    // ─────────────────────────────────────────────

    public CompletableFuture<String> heavyComputationThenFormat(int n) {
        return CompletableFuture
                .supplyAsync(() -> simulateHeavyWork(n), executor)
                .thenApply(result -> "Результат вычисления: " + result);
    }

    // ─────────────────────────────────────────────
    // 3. Параллельный запуск нескольких тяжёлых задач
    //
    // Все задачи стартуют одновременно. Ждём завершения всех через allOf().
    // Итоговое время ≈ время самой долгой задачи, а не сумма всех.
    // ─────────────────────────────────────────────

    public CompletableFuture<List<Long>> runAllInParallel(List<Integer> inputs) {
        List<CompletableFuture<Long>> futures = inputs.stream()
                .map(n -> CompletableFuture.supplyAsync(() -> simulateHeavyWork(n), executor))
                .toList();

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream()
                        .map(CompletableFuture::join)
                        .toList());
    }

    // ─────────────────────────────────────────────
    // 4. Первый готовый результат (anyOf)
    //
    // Запускаем несколько реплик одной задачи — берём того, кто ответил быстрее.
    // Полезно для hedged requests (дублирующие запросы к разным серверам).
    // ─────────────────────────────────────────────

    public CompletableFuture<Object> firstResult(List<Supplier<Long>> tasks) {
        List<CompletableFuture<Long>> futures = tasks.stream()
                .map(task -> CompletableFuture.supplyAsync(task, executor))
                .toList();

        return CompletableFuture.anyOf(futures.toArray(new CompletableFuture[0]));
    }

    // ─────────────────────────────────────────────
    // 5. Обработка ошибок в асинхронном коде
    //
    // exceptionally() — fallback при исключении, не ломает цепочку.
    // ─────────────────────────────────────────────

    public CompletableFuture<String> safeAsyncCall(boolean shouldFail) {
        return CompletableFuture
                .supplyAsync(() -> {
                    if (shouldFail) throw new RuntimeException("Что-то пошло не так");
                    return "Успех";
                }, executor)
                .exceptionally(ex -> "Fallback: " + ex.getMessage());
    }

    // ─────────────────────────────────────────────
    // 6. Таймаут на асинхронную операцию (Java 9+)
    //
    // Если задача не завершилась за отведённое время — получаем исключение.
    // ─────────────────────────────────────────────

    public CompletableFuture<Long> withTimeout(int n, long timeoutMs) {
        return CompletableFuture
                .supplyAsync(() -> simulateHeavyWork(n), executor)
                .orTimeout(timeoutMs, TimeUnit.MILLISECONDS);
    }

    // ─────────────────────────────────────────────
    // 7. thenCompose — последовательные асинхронные шаги
    //
    // Когда второй шаг зависит от результата первого,
    // но оба должны выполняться асинхронно.
    // ─────────────────────────────────────────────

    public CompletableFuture<String> twoStepPipeline(int n) {
        return CompletableFuture
                .supplyAsync(() -> simulateHeavyWork(n), executor)          // шаг 1
                .thenCompose(result ->
                        CompletableFuture.supplyAsync(                       // шаг 2
                                () -> postProcess(result), executor));
    }

    // ── Вспомогательные методы ────────────────────

    long simulateHeavyWork(int n) {
        // Имитация тяжёлого CPU-вычисления: сумма от 1 до n
        long sum = 0;
        for (int i = 1; i <= n; i++) sum += i;
        return sum;
    }

    private String postProcess(long value) {
        return "Обработано: " + (value * 2);
    }
}
