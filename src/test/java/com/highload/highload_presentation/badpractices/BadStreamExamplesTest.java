package com.highload.highload_presentation.badpractices;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BadStreamExamplesTest {

    private BadStreamExamples examples;

    @BeforeEach
    void setUp() {
        examples = new BadStreamExamples();
    }

    // ── 1. Несколько filter vs один filter ────────

    @Test
    void badAndGoodFilter_returnSameResult() {
        List<String> input = Arrays.asList("hi", null, "  ", "hello", "world", "ok");
        assertEquals(
                examples.goodSingleFilter(input),
                examples.badMultipleFilters(input)
        );
    }

    @Test
    void goodSingleFilter_removesNullBlankAndShort() {
        List<String> input = Arrays.asList("hello", null, "", "  ", "hi", "world");
        List<String> result = examples.goodSingleFilter(input);
        assertEquals(List.of("hello", "world"), result);
    }

    // ── 2. sorted() + limit() ─────────────────────

    @Test
    void badAndGoodSortLimit_returnSameElements() {
        List<Integer> numbers = List.of(5, 3, 8, 1, 9, 2, 7, 4, 6);
        List<Integer> bad  = examples.badSortThenLimit(numbers, 3);
        List<Integer> good = examples.goodLimitThenSort(numbers, 3);
        assertEquals(bad, good);
    }

    @Test
    void goodLimitThenSort_returnsSmallestN() {
        List<Integer> numbers = List.of(10, 3, 7, 1, 5);
        List<Integer> result = examples.goodLimitThenSort(numbers, 2);
        assertEquals(List.of(1, 3), result);
    }

    // ── 3. Вложенный стрим vs flatMap ─────────────

    @Test
    void badAndGoodFlatMap_returnSameResult() {
        List<List<Integer>> matrix = List.of(
                List.of(1, -2, 3),
                List.of(-4, 5, 0),
                List.of(6, -7, 8)
        );
        List<Integer> bad  = examples.badNestedStream(matrix);
        List<Integer> good = examples.goodFlatMap(matrix);
        assertEquals(bad, good);
    }

    @Test
    void goodFlatMap_filtersNegativesAndZeroes() {
        List<List<Integer>> matrix = List.of(List.of(-1, 2, 0, 3));
        assertEquals(List.of(2, 3), examples.goodFlatMap(matrix));
    }

    // ── 4. count() без collect() ──────────────────

    @Test
    void badAndGoodCount_returnSameValue() {
        List<String> items = List.of("Apple", "Banana", "Avocado", "Cherry", "Apricot");
        assertEquals(examples.goodCount(items), examples.badCount(items));
    }

    @Test
    void goodCount_countsCorrectly() {
        List<String> items = List.of("Apple", "Banana", "Avocado", "Cherry");
        assertEquals(2, examples.goodCount(items));
    }

    @Test
    void goodCount_returnsZeroWhenNoneMatch() {
        assertEquals(0, examples.goodCount(List.of("Banana", "Cherry")));
    }

    // ── 5. peek() vs map() ────────────────────────

    @Test
    void goodMapForTransform_trimsAllElements() {
        List<String> input = List.of("  hello  ", " world", "java ");
        List<String> result = examples.goodMapForTransform(input);
        assertEquals(List.of("hello", "world", "java"), result);
    }

    // ── 6. anyMatch vs collect + isEmpty ──────────

    @Test
    void badAndGoodAnyMatch_returnSameResult() {
        List<Integer> numbers = List.of(10, 50, 150, 200);
        assertEquals(examples.goodAnyMatch(numbers), examples.badAnyMatch(numbers));
    }

    @Test
    void goodAnyMatch_returnsTrueWhenExists() {
        assertTrue(examples.goodAnyMatch(List.of(50, 101, 200)));
    }

    @Test
    void goodAnyMatch_returnsFalseWhenNoneMatch() {
        assertFalse(examples.goodAnyMatch(List.of(1, 50, 99)));
    }

    // ── 7. partitioningBy vs двойной проход ───────

    @Test
    void badAndGoodDoubleStream_returnSameResult() {
        List<Integer> numbers = List.of(-3, -1, 0, 2, 4, 6);
        Map<Boolean, Long> bad  = examples.badDoubleStream(numbers);
        Map<Boolean, Long> good = examples.goodPartitioningBy(numbers);
        assertEquals(bad.get(true),  good.get(true));
        assertEquals(bad.get(false), good.get(false));
    }

    @Test
    void goodPartitioningBy_countsCorrectly() {
        List<Integer> numbers = List.of(-2, -1, 0, 1, 2, 3);
        Map<Boolean, Long> result = examples.goodPartitioningBy(numbers);
        assertEquals(3L, result.get(true));  // 1, 2, 3
        assertEquals(2L, result.get(false)); // -2, -1
    }

    // ── 8. toList() vs unmodifiableList ───────────

    @Test
    void badAndGoodUnmodifiable_returnSameElements() {
        List<String> input = List.of("a", "", "b", "c", "");
        assertEquals(
                examples.goodUnmodifiable(input),
                examples.badUnmodifiable(input)
        );
    }

    @Test
    void goodUnmodifiable_returnsImmutableList() {
        List<String> result = examples.goodUnmodifiable(List.of("a", "b"));
        assertThrows(UnsupportedOperationException.class, () -> result.add("c"));
    }

    @Test
    void goodUnmodifiable_filtersEmptyStrings() {
        List<String> result = examples.goodUnmodifiable(List.of("hello", "", "world", ""));
        assertEquals(List.of("hello", "world"), result);
    }
}
