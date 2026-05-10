package com.highload.highload_presentation.cpu;

public class PrimeCalculator {

    /**
     * Неоптимизированный подсчёт простых чисел до n.
     * Проверяет делимость на все числа от 2 до n-1.
     */
    public int countPrimesUnoptimized(int n) {
        int count = 0;
        for (int i = 2; i <= n; i++) {
            if (isPrimeNaive(i)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Оптимизированный подсчёт простых чисел до n.
     * Решето Эратосфена — O(n log log n).
     */
    public int countPrimesOptimized(int n) {
        if (n < 2) return 0;
        boolean[] isComposite = new boolean[n + 1];
        for (int i = 2; (long) i * i <= n; i++) {
            if (!isComposite[i]) {
                for (int j = i * i; j <= n; j += i) {
                    isComposite[j] = true;
                }
            }
        }
        int count = 0;
        for (int i = 2; i <= n; i++) {
            if (!isComposite[i]) {
                count++;
            }
        }
        return count;
    }

    private boolean isPrimeNaive(int num) {
        if (num < 2) return false;
        for (int i = 2; i < num; i++) {
            if (num % i == 0) return false;
        }
        return true;
    }
}
