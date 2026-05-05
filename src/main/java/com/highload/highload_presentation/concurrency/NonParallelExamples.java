package com.highload.highload_presentation.concurrency;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Примеры задач, которые НЕЛЬЗЯ (или сложно) распараллелить.
 *
 * Задача НЕ подходит для параллелизации если:
 *  - Каждый шаг зависит от результата предыдущего (зависимость по данным)
 *  - Есть общее изменяемое состояние (shared mutable state)
 *  - Порядок операций важен и не может быть нарушен
 *  - Накладные расходы на синхронизацию превышают выигрыш от параллелизма
 */
public class NonParallelExamples {

    // ─────────────────────────────────────────────
    // ❌ Числа Фибоначчи (последовательная зависимость)
    // F(n) = F(n-1) + F(n-2) — каждое значение зависит от двух предыдущих.
    // Нельзя вычислить F(10), не зная F(9) и F(8).
    // ─────────────────────────────────────────────

    public long fibonacci(int n) {
        if (n <= 1) return n;
        long prev2 = 0, prev1 = 1;
        for (int i = 2; i <= n; i++) {
            long current = prev1 + prev2; // зависит от предыдущего шага
            prev2 = prev1;
            prev1 = current;
        }
        return prev1;
    }

    // ─────────────────────────────────────────────
    // ❌ Связный список — последовательный обход
    // Чтобы найти следующий узел, нужно знать текущий.
    // Нет случайного доступа — параллельный обход невозможен.
    // ─────────────────────────────────────────────

    static class Node {
        int value;
        Node next;
        Node(int value) { this.value = value; }
    }

    public int sumLinkedList(Node head) {
        int sum = 0;
        Node current = head;
        while (current != null) {
            sum += current.value;   // каждый шаг зависит от предыдущего
            current = current.next;
        }
        return sum;
    }

    // ─────────────────────────────────────────────
    // ❌ Общий счётчик без синхронизации — Race Condition
    // Несколько потоков читают и пишут одну переменную одновременно.
    // Результат непредсказуем — это баг, а не фича.
    // ─────────────────────────────────────────────

    private int unsafeCounter = 0;

    public void demonstrateRaceCondition() throws InterruptedException {
        unsafeCounter = 0;
        List<Thread> threads = new ArrayList<>();

        for (int i = 0; i < 1000; i++) {
            threads.add(Thread.ofVirtual().start(() -> {
                unsafeCounter++; // ❌ read-modify-write не атомарна!
            }));
        }

        for (Thread t : threads) t.join();

        // unsafeCounter скорее всего НЕ равен 1000
        System.out.println("Unsafe counter (ожидаем 1000): " + unsafeCounter);
    }

    // Правильное решение — AtomicInteger:
    private final AtomicInteger safeCounter = new AtomicInteger(0);

    public void demonstrateSafeCounter() throws InterruptedException {
        safeCounter.set(0);
        List<Thread> threads = new ArrayList<>();

        for (int i = 0; i < 1000; i++) {
            threads.add(Thread.ofVirtual().start(() -> {
                safeCounter.incrementAndGet(); // ✅ атомарная операция
            }));
        }

        for (Thread t : threads) t.join();
        System.out.println("Safe counter (ожидаем 1000): " + safeCounter.get());
    }

    // ─────────────────────────────────────────────
    // ❌ Накопление в общий список без синхронизации
    // ArrayList не потокобезопасен — параллельная запись ломает структуру.
    // ─────────────────────────────────────────────

    public List<Integer> unsafeParallelCollect(int n) throws InterruptedException {
        List<Integer> results = new ArrayList<>(); // ❌ не потокобезопасен
        List<Thread> threads = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            final int val = i;
            threads.add(Thread.ofVirtual().start(() -> results.add(val)));
        }

        for (Thread t : threads) t.join();
        // results.size() может быть меньше n или выброситься ArrayIndexOutOfBoundsException
        return results;
    }

    // Правильное решение — CopyOnWriteArrayList или сбор через Stream:
    public List<Integer> safeParallelCollect(int n) {
        return IntStream.range(0, n)
                .parallel()
                .boxed()
                .collect(Collectors.toList()); // ✅ thread-safe сбор
    }

    // ─────────────────────────────────────────────
    // ❌ Транзакционные операции с БД
    // Параллельные транзакции могут конфликтовать (dirty read, lost update).
    // Решается через уровни изоляции и пессимистичные/оптимистичные блокировки.
    // Здесь — имитация проблемы lost update:
    // ─────────────────────────────────────────────

    private int bankBalance = 1000;
    private final ReentrantLock lock = new ReentrantLock();

    public void unsafeWithdraw(int amount) {
        if (bankBalance >= amount) {          // ❌ между проверкой и списанием
            bankBalance -= amount;            //    другой поток может изменить баланс
        }
    }

    public void safeWithdraw(int amount) {
        lock.lock();
        try {
            if (bankBalance >= amount) {      // ✅ атомарная проверка + списание
                bankBalance -= amount;
            }
        } finally {
            lock.unlock();
        }
    }

    // ─────────────────────────────────────────────
    // ❌ Мелкие задачи — накладные расходы превышают выигрыш
    // Создание потока стоит дорого. Для простого сложения двух чисел
    // параллелизм только замедлит программу.
    // ─────────────────────────────────────────────

    public int sumTwoNumbers(int a, int b) {
        // ❌ НЕ стоит делать это в отдельном потоке
        return a + b;
    }
}
