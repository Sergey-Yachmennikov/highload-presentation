package com.highload.highload_presentation.complexity;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Примеры алгоритмов для каждого класса сложности Big O Notation.
 */
public class BigOExamples {

    // ─────────────────────────────────────────────
    // O(1) — Константная сложность
    // Время выполнения не зависит от размера входных данных.
    // Примеры: доступ к элементу массива по индексу, операции с HashMap.
    // ─────────────────────────────────────────────

    public int getFirstElement(int[] array) {
        return array[0];
    }

    public boolean containsKey(Map<String, Integer> map, String key) {
        return map.containsKey(key);
    }

    // ─────────────────────────────────────────────
    // O(log n) — Логарифмическая сложность
    // Каждый шаг делит задачу пополам.
    // Примеры: бинарный поиск, операции в сбалансированном дереве.
    // ─────────────────────────────────────────────

    public int binarySearch(int[] sortedArray, int target) {
        int left = 0, right = sortedArray.length - 1;
        while (left <= right) {
            int mid = left + (right - left) / 2;
            if (sortedArray[mid] == target) return mid;
            if (sortedArray[mid] < target) left = mid + 1;
            else right = mid - 1;
        }
        return -1;
    }

    // ─────────────────────────────────────────────
    // O(n) — Линейная сложность
    // Время растёт пропорционально размеру входных данных.
    // Примеры: линейный поиск, обход массива/списка.
    // ─────────────────────────────────────────────

    public int linearSearch(int[] array, int target) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == target) return i;
        }
        return -1;
    }

    public int sumArray(int[] array) {
        int sum = 0;
        for (int value : array) {
            sum += value;
        }
        return sum;
    }

    // ─────────────────────────────────────────────
    // O(n log n) — Линеарифмическая сложность
    // Типична для эффективных алгоритмов сортировки.
    // Примеры: merge sort, heap sort, Arrays.sort().
    // ─────────────────────────────────────────────

    public int[] mergeSort(int[] array) {
        if (array.length <= 1) return array;

        int mid = array.length / 2;
        int[] left = mergeSort(Arrays.copyOfRange(array, 0, mid));
        int[] right = mergeSort(Arrays.copyOfRange(array, mid, array.length));
        return merge(left, right);
    }

    private int[] merge(int[] left, int[] right) {
        int[] result = new int[left.length + right.length];
        int i = 0, j = 0, k = 0;
        while (i < left.length && j < right.length) {
            if (left[i] <= right[j]) result[k++] = left[i++];
            else result[k++] = right[j++];
        }
        while (i < left.length) result[k++] = left[i++];
        while (j < right.length) result[k++] = right[j++];
        return result;
    }

    // ─────────────────────────────────────────────
    // O(n²) — Квадратичная сложность
    // Два вложенных цикла по n элементам.
    // Примеры: bubble sort, insertion sort, поиск всех пар.
    // ─────────────────────────────────────────────

    public int[] bubbleSort(int[] array) {
        int[] arr = Arrays.copyOf(array, array.length);
        for (int i = 0; i < arr.length - 1; i++) {
            for (int j = 0; j < arr.length - 1 - i; j++) {
                if (arr[j] > arr[j + 1]) {
                    int temp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = temp;
                }
            }
        }
        return arr;
    }

    public List<int[]> findAllPairs(int[] array) {
        List<int[]> pairs = new java.util.ArrayList<>();
        for (int i = 0; i < array.length; i++) {
            for (int j = i + 1; j < array.length; j++) {
                pairs.add(new int[]{array[i], array[j]});
            }
        }
        return pairs;
    }

    // ─────────────────────────────────────────────
    // O(n³) — Кубическая сложность
    // Три вложенных цикла. Встречается в задачах на матрицы.
    // Примеры: умножение матриц (naive), поиск всех троек.
    // ─────────────────────────────────────────────

    public int[][] multiplyMatrices(int[][] a, int[][] b) {
        int n = a.length;
        int[][] result = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                for (int k = 0; k < n; k++) {
                    result[i][j] += a[i][k] * b[k][j];
                }
            }
        }
        return result;
    }

    // ─────────────────────────────────────────────
    // O(2ⁿ) — Экспоненциальная сложность
    // Каждый новый элемент удваивает количество операций.
    // Примеры: наивный рекурсивный Фибоначчи, задача о рюкзаке (brute force).
    // ─────────────────────────────────────────────

    public long fibonacciExponential(int n) {
        if (n <= 1) return n;
        return fibonacciExponential(n - 1) + fibonacciExponential(n - 2);
    }

    // ─────────────────────────────────────────────
    // O(n!) — Факториальная сложность
    // Генерация всех перестановок. Растёт катастрофически быстро.
    // Примеры: задача коммивояжёра (brute force), генерация всех перестановок.
    // ─────────────────────────────────────────────

    public List<List<Integer>> generatePermutations(List<Integer> elements) {
        List<List<Integer>> result = new java.util.ArrayList<>();
        permute(new java.util.ArrayList<>(elements), 0, result);
        return result;
    }

    private void permute(List<Integer> arr, int start, List<List<Integer>> result) {
        if (start == arr.size()) {
            result.add(new java.util.ArrayList<>(arr));
            return;
        }
        for (int i = start; i < arr.size(); i++) {
            java.util.Collections.swap(arr, start, i);
            permute(arr, start + 1, result);
            java.util.Collections.swap(arr, start, i);
        }
    }

    // ─────────────────────────────────────────────
    // Бонус: O(n) с использованием доп. памяти O(n)
    // Фибоначчи через динамическое программирование — O(n) время, O(n) память.
    // ─────────────────────────────────────────────

    public long fibonacciLinear(int n) {
        if (n <= 1) return n;
        Map<Integer, Long> memo = new HashMap<>();
        memo.put(0, 0L);
        memo.put(1, 1L);
        for (int i = 2; i <= n; i++) {
            memo.put(i, memo.get(i - 1) + memo.get(i - 2));
        }
        return memo.get(n);
    }
}
