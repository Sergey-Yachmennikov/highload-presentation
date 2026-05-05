package com.highload.highload_presentation.complexity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BigOExamplesTest {

    private BigOExamples examples;

    @BeforeEach
    void setUp() {
        examples = new BigOExamples();
    }

    // ── O(1) ──────────────────────────────────────

    @Test
    void getFirstElement_returnsFirstElement() {
        assertEquals(42, examples.getFirstElement(new int[]{42, 1, 2, 3}));
    }

    @Test
    void containsKey_returnsTrueWhenKeyExists() {
        assertTrue(examples.containsKey(java.util.Map.of("a", 1), "a"));
    }

    @Test
    void containsKey_returnsFalseWhenKeyAbsent() {
        assertFalse(examples.containsKey(java.util.Map.of("a", 1), "b"));
    }

    // ── O(log n) ──────────────────────────────────

    @Test
    void binarySearch_findsExistingElement() {
        int[] sorted = {1, 3, 5, 7, 9, 11, 13};
        assertEquals(3, examples.binarySearch(sorted, 7));
    }

    @Test
    void binarySearch_returnsMinusOneWhenNotFound() {
        int[] sorted = {1, 3, 5, 7, 9};
        assertEquals(-1, examples.binarySearch(sorted, 4));
    }

    @Test
    void binarySearch_findsFirstElement() {
        int[] sorted = {2, 4, 6, 8};
        assertEquals(0, examples.binarySearch(sorted, 2));
    }

    @Test
    void binarySearch_findsLastElement() {
        int[] sorted = {2, 4, 6, 8};
        assertEquals(3, examples.binarySearch(sorted, 8));
    }

    // ── O(n) ──────────────────────────────────────

    @Test
    void linearSearch_findsElement() {
        assertEquals(2, examples.linearSearch(new int[]{10, 20, 30, 40}, 30));
    }

    @Test
    void linearSearch_returnsMinusOneWhenNotFound() {
        assertEquals(-1, examples.linearSearch(new int[]{1, 2, 3}, 99));
    }

    @Test
    void sumArray_returnsCorrectSum() {
        assertEquals(15, examples.sumArray(new int[]{1, 2, 3, 4, 5}));
    }

    @Test
    void sumArray_returnsZeroForEmptyArray() {
        assertEquals(0, examples.sumArray(new int[]{}));
    }

    // ── O(n log n) ────────────────────────────────

    @Test
    void mergeSort_sortsSingleElement() {
        assertArrayEquals(new int[]{5}, examples.mergeSort(new int[]{5}));
    }

    @Test
    void mergeSort_sortsUnsortedArray() {
        assertArrayEquals(new int[]{1, 2, 3, 4, 5},
                examples.mergeSort(new int[]{3, 1, 4, 5, 2}));
    }

    @Test
    void mergeSort_handlesAlreadySortedArray() {
        assertArrayEquals(new int[]{1, 2, 3},
                examples.mergeSort(new int[]{1, 2, 3}));
    }

    @Test
    void mergeSort_handlesReverseSortedArray() {
        assertArrayEquals(new int[]{1, 2, 3, 4},
                examples.mergeSort(new int[]{4, 3, 2, 1}));
    }

    // ── O(n²) ─────────────────────────────────────

    @Test
    void bubbleSort_sortsArray() {
        assertArrayEquals(new int[]{1, 2, 3, 4, 5},
                examples.bubbleSort(new int[]{5, 3, 1, 4, 2}));
    }

    @Test
    void bubbleSort_doesNotMutateOriginalArray() {
        int[] original = {3, 1, 2};
        examples.bubbleSort(original);
        assertArrayEquals(new int[]{3, 1, 2}, original);
    }

    @Test
    void findAllPairs_returnsCorrectCount() {
        // Для n=4 элементов: C(4,2) = 6 пар
        List<int[]> pairs = examples.findAllPairs(new int[]{1, 2, 3, 4});
        assertEquals(6, pairs.size());
    }

    @Test
    void findAllPairs_returnsOnePairForTwoElements() {
        List<int[]> pairs = examples.findAllPairs(new int[]{1, 2});
        assertEquals(1, pairs.size());
        assertArrayEquals(new int[]{1, 2}, pairs.get(0));
    }

    // ── O(n³) ─────────────────────────────────────

    @Test
    void multiplyMatrices_returnsCorrectResult() {
        int[][] a = {{1, 2}, {3, 4}};
        int[][] b = {{5, 6}, {7, 8}};
        int[][] expected = {{19, 22}, {43, 50}};
        assertArrayEquals(expected, examples.multiplyMatrices(a, b));
    }

    @Test
    void multiplyMatrices_identityMatrix() {
        int[][] a = {{1, 2}, {3, 4}};
        int[][] identity = {{1, 0}, {0, 1}};
        assertArrayEquals(a, examples.multiplyMatrices(a, identity));
    }

    // ── O(2ⁿ) ─────────────────────────────────────

    @Test
    void fibonacciExponential_baseCase() {
        assertEquals(0, examples.fibonacciExponential(0));
        assertEquals(1, examples.fibonacciExponential(1));
    }

    @Test
    void fibonacciExponential_smallValues() {
        assertEquals(8, examples.fibonacciExponential(6));
        assertEquals(55, examples.fibonacciExponential(10));
    }

    // ── O(n!) ─────────────────────────────────────

    @Test
    void generatePermutations_countIsFactorial() {
        // 3! = 6
        List<List<Integer>> perms = examples.generatePermutations(List.of(1, 2, 3));
        assertEquals(6, perms.size());
    }

    @Test
    void generatePermutations_singleElement() {
        List<List<Integer>> perms = examples.generatePermutations(List.of(1));
        assertEquals(1, perms.size());
        assertEquals(List.of(1), perms.get(0));
    }

    // ── O(n) Fibonacci DP ─────────────────────────

    @Test
    void fibonacciLinear_matchesExponentialResults() {
        for (int i = 0; i <= 15; i++) {
            assertEquals(examples.fibonacciExponential(i), examples.fibonacciLinear(i),
                    "Mismatch at n=" + i);
        }
    }
}
