package com.highload.highload_presentation.badpractices;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class BadInitializationExamplesTest {

    private BadInitializationExamples examples;

    @BeforeEach
    void setUp() {
        examples = new BadInitializationExamples();
    }

    // ── 1. Магические числа ───────────────────────

    @Test
    void badDiscount_andGoodDiscount_returnSameResult() {
        assertEquals(examples.goodDiscount(100.0), examples.badDiscount(100.0), 0.0001);
    }

    @Test
    void goodDiscount_appliesCorrectRate() {
        assertEquals(85.0, examples.goodDiscount(100.0), 0.0001);
    }

    // ── 2. Pattern.compile() внутри цикла ────────

    @Test
    void badAndGoodPatternInLoop_returnSameResult() {
        List<String> inputs = List.of("hello", "foo123", "42", "world", "test7");
        assertEquals(examples.goodPatternInLoop(inputs), examples.badPatternInLoop(inputs));
    }

    @Test
    void goodPatternInLoop_returnsOnlyStringsWithDigits() {
        List<String> inputs = List.of("hello", "foo123", "42", "world", "test7");
        assertEquals(List.of("foo123", "42", "test7"), examples.goodPatternInLoop(inputs));
    }

    @Test
    void goodPatternInLoop_emptyListReturnsEmptyList() {
        assertTrue(examples.goodPatternInLoop(List.of()).isEmpty());
    }

    // ── 3. String конкатенация в цикле ────────────

    @Test
    void badAndGoodConcatenate_returnSameResult() {
        List<String> parts = List.of("a", "b", "c", "d");
        assertEquals(examples.goodConcatenate(parts), examples.badConcatenate(parts));
    }

    @Test
    void goodConcatenate_joinsAllParts() {
        assertEquals("hello", examples.goodConcatenate(List.of("he", "ll", "o")));
    }

    @Test
    void goodConcatenate_emptyListReturnsEmptyString() {
        assertEquals("", examples.goodConcatenate(List.of()));
    }

    // ── 4. Начальный размер коллекции ─────────────

    @Test
    void badAndGoodPreallocate_returnSameResult() {
        int[] source = {1, 2, 3, 4, 5};
        assertEquals(examples.goodPreallocate(source), examples.badPreallocate(source));
    }

    @Test
    void goodPreallocate_containsAllElements() {
        int[] source = {10, 20, 30};
        List<Integer> result = examples.goodPreallocate(source);
        assertEquals(List.of(10, 20, 30), result);
    }

    // ── 5. Double vs BigDecimal для денег ─────────

    @Test
    void badMoneySum_hasFloatingPointError() {
        // 0.1 + 0.2 в double НЕ равно 0.3
        assertNotEquals(0.3, examples.badMoneySum());
    }

    @Test
    void goodMoneySum_returnsExactResult() {
        assertEquals(new BigDecimal("0.3"), examples.goodMoneySum());
    }

    // ── 6. Boxing antipattern ─────────────────────

    @Test
    void badBoxing_referenceEqualityReturnsFalse() {
        assertFalse(examples.badBoxing());
    }

    @Test
    void goodBoxing_valueEqualityReturnsTrue() {
        assertTrue(examples.goodBoxing());
    }

    // ── 7. Optional ───────────────────────────────

    @Test
    void badOptional_throwsNpeOnNull() {
        assertThrows(NullPointerException.class, () -> examples.badOptional(null));
    }

    @Test
    void goodOptional_returnsEmptyForNull() {
        assertEquals(Optional.empty(), examples.goodOptional(null));
    }

    @Test
    void goodOptional_returnsPresentForNonNull() {
        assertEquals(Optional.of("hello"), examples.goodOptional("hello"));
    }

    // ── 8. Random ─────────────────────────────────

    @Test
    void goodRandom_returnsValueWithinBound() {
        for (int i = 0; i < 100; i++) {
            int value = examples.goodRandom(10);
            assertTrue(value >= 0 && value < 10);
        }
    }
}
