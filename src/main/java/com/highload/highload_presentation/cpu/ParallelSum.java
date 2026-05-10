package com.highload.highload_presentation.cpu;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class ParallelSum {

    /**
     * Однопоточное суммирование: вычисляет сумму тяжёлых операций над элементами массива.
     */
    public long sumSingleThread(int[] array) {
        long sum = 0;
        for (int value : array) {
            sum += heavyComputation(value);
        }
        return sum;
    }

    /**
     * Многопоточное суммирование через ForkJoinPool.
     * Разбивает массив на части и считает параллельно.
     */
    public long sumParallel(int[] array) {
        try (ForkJoinPool pool = new ForkJoinPool()) {
            return pool.invoke(new SumTask(array, 0, array.length));
        }
    }

    private static long heavyComputation(int value) {
        // Имитация CPU-нагрузки: многократное вычисление
        double result = value;
        for (int i = 0; i < 1000; i++) {
            result = Math.sin(result) + Math.cos(result);
        }
        return (long) result + value;
    }

    private static class SumTask extends RecursiveTask<Long> {
        private static final int THRESHOLD = 10_000;
        private final int[] array;
        private final int from;
        private final int to;

        SumTask(int[] array, int from, int to) {
            this.array = array;
            this.from = from;
            this.to = to;
        }

        @Override
        protected Long compute() {
            if (to - from <= THRESHOLD) {
                long sum = 0;
                for (int i = from; i < to; i++) {
                    sum += heavyComputation(array[i]);
                }
                return sum;
            }
            int mid = (from + to) / 2;
            SumTask left = new SumTask(array, from, mid);
            SumTask right = new SumTask(array, mid, to);
            left.fork();
            long rightResult = right.compute();
            long leftResult = left.join();
            return leftResult + rightResult;
        }
    }
}
