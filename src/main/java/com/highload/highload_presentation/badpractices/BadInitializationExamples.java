package com.highload.highload_presentation.badpractices;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Примеры неправильных инициализаций переменных и их правильные альтернативы.
 */
public class BadInitializationExamples {

    // ─────────────────────────────────────────────
    // 1. Магические числа вместо констант
    //
    // ❌ Смысл числа непонятен, при изменении нужно искать по всему коду.
    // ✅ Именованная константа — самодокументируется, меняется в одном месте.
    // ─────────────────────────────────────────────

    public double badDiscount(double price) {
        return price * 0.85; // ❌ что такое 0.85?
    }

    private static final double DISCOUNT_RATE = 0.85;

    public double goodDiscount(double price) {
        return price * DISCOUNT_RATE; // ✅
    }

    // ─────────────────────────────────────────────
    // 2. Компиляция регулярного выражения внутри цикла
    //
    // ❌ Pattern.compile() внутри цикла — компиляция regex дорогостоящая операция,
    //    выполняется заново на каждой итерации.
    // ✅ Компилируем один раз в static final поле — переиспользуем.
    // ─────────────────────────────────────────────

    public List<String> badPatternInLoop(List<String> inputs) {
        List<String> result = new ArrayList<>();
        for (String input : inputs) {
            Pattern pattern = Pattern.compile("\\d+"); // ❌ компиляция на каждой итерации
            if (pattern.matcher(input).find()) {
                result.add(input);
            }
        }
        return result;
    }

    private static final Pattern DIGITS_PATTERN = Pattern.compile("\\d+"); // ✅ один раз

    public List<String> goodPatternInLoop(List<String> inputs) {
        List<String> result = new ArrayList<>();
        for (String input : inputs) {
            if (DIGITS_PATTERN.matcher(input).find()) { // ✅ переиспользуем скомпилированный паттерн
                result.add(input);
            }
        }
        return result;
    }

    // ─────────────────────────────────────────────
    // 3. String конкатенация в цикле
    //
    // ❌ Каждый += создаёт новый объект String → O(n²) по памяти.
    // ✅ StringBuilder даёт O(n).
    // ─────────────────────────────────────────────

    public String badConcatenate(List<String> parts) {
        String result = ""; // ❌
        for (String part : parts) {
            result += part; // ❌ новая строка на каждой итерации
        }
        return result;
    }

    public String goodConcatenate(List<String> parts) {
        StringBuilder sb = new StringBuilder(); // ✅
        for (String part : parts) {
            sb.append(part);
        }
        return sb.toString();
    }

    // ─────────────────────────────────────────────
    // 4. Неправильный начальный размер коллекции
    //
    // ❌ ArrayList() без capacity → при заполнении происходят costly resize-операции.
    // ✅ Если размер известен — передаём его сразу.
    // ─────────────────────────────────────────────

    public List<Integer> badPreallocate(int[] source) {
        List<Integer> list = new ArrayList<>(); // ❌ capacity=10 по умолчанию
        for (int val : source) list.add(val);
        return list;
    }

    public List<Integer> goodPreallocate(int[] source) {
        List<Integer> list = new ArrayList<>(source.length); // ✅ capacity=n сразу
        for (int val : source) list.add(val);
        return list;
    }

    // ─────────────────────────────────────────────
    // 5. Double для денежных вычислений
    //
    // ❌ double/float — числа с плавающей точкой, финансовые вычисления теряют точность.
    // ✅ BigDecimal гарантирует точность.
    // ─────────────────────────────────────────────

    public double badMoneySum() {
        double a = 0.1;
        double b = 0.2;
        return a + b; // ❌ вернёт 0.30000000000000004
    }

    public BigDecimal goodMoneySum() {
        BigDecimal a = new BigDecimal("0.1"); // ✅ строковый литерал — важно!
        BigDecimal b = new BigDecimal("0.2");
        return a.add(b); // ✅ вернёт 0.3
    }

    // ─────────────────────────────────────────────
    // 6. Инициализация через new Boolean / new Integer (boxing antipattern)
    //
    // ❌ new Integer(42) создаёт новый объект в heap, не использует кэш.
    // ✅ Integer.valueOf(42) использует кэш [-128..127].
    // ─────────────────────────────────────────────

    @SuppressWarnings("deprecation")
    public boolean badBoxing() {
        Integer a = new Integer(42);  // ❌ deprecated, всегда новый объект
        Integer b = new Integer(42);
        return a == b; // ❌ false — разные объекты в heap
    }

    public boolean goodBoxing() {
        Integer a = Integer.valueOf(42); // ✅ берёт из кэша
        Integer b = Integer.valueOf(42);
        return a.equals(b); // ✅ true
    }

    // ─────────────────────────────────────────────
    // 7. Инициализация Optional неправильно
    //
    // ❌ Optional.of(null) бросает NullPointerException сразу.
    // ✅ Optional.ofNullable(value) — безопасно обрабатывает null.
    // ─────────────────────────────────────────────

    public Optional<String> badOptional(String value) {
        return Optional.of(value); // ❌ NPE если value == null
    }

    public Optional<String> goodOptional(String value) {
        return Optional.ofNullable(value); // ✅ вернёт Optional.empty() для null
    }

    // ─────────────────────────────────────────────
    // 8. Повторная инициализация вместо переиспользования
    //
    // ❌ Random создаётся каждый раз — это дорого и нарушает равномерность.
    // ✅ Один экземпляр на класс (или ThreadLocalRandom для многопоточности).
    // ─────────────────────────────────────────────

    public int badRandom(int bound) {
        return new Random().nextInt(bound); // ❌ новый Random на каждый вызов
    }

    private static final Random RANDOM = new Random(); // ✅ один экземпляр

    public int goodRandom(int bound) {
        return RANDOM.nextInt(bound); // ✅
    }
}
