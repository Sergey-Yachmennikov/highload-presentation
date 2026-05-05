package com.highload.highload_presentation.badpractices;

import java.util.*;
import java.util.stream.*;

/**
 * Примеры неэффективного использования Stream API и их правильные альтернативы.
 */
public class BadStreamExamples {

    // ─────────────────────────────────────────────
    // 1. Несколько проходов вместо одного
    //
    // ❌ filter + map + filter — три прохода по стриму (три промежуточные коллекции).
    // ✅ Объединяем условия в одну filter-цепочку — один проход.
    // ─────────────────────────────────────────────

    public List<String> badMultipleFilters(List<String> items) {
        return items.stream()
                .filter(s -> s != null)          // ❌ проход 1
                .filter(s -> !s.isBlank())        // ❌ проход 2
                .filter(s -> s.length() > 3)      // ❌ проход 3
                .toList();
    }

    public List<String> goodSingleFilter(List<String> items) {
        return items.stream()
                .filter(s -> s != null && !s.isBlank() && s.length() > 3) // ✅ один проход
                .toList();
    }

    // ─────────────────────────────────────────────
    // 2. collect(toList()) после sorted() на большой коллекции без необходимости
    //
    // ❌ sorted() без limit() сортирует весь список ради первых N элементов.
    // ✅ Сначала limit(), потом sorted() — сортируем только нужное.
    // ─────────────────────────────────────────────

    public List<Integer> badSortThenLimit(List<Integer> numbers, int n) {
        return numbers.stream()
                .sorted()          // ❌ сортируем весь список
                .limit(n)          // ❌ берём только n
                .toList();
    }

    public List<Integer> goodLimitThenSort(List<Integer> numbers, int n) {
        // Если нужны наименьшие n элементов — PriorityQueue O(n + k log n)
        // вместо полной сортировки O(n log n)
        PriorityQueue<Integer> pq = new PriorityQueue<>(numbers);
        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < n && !pq.isEmpty(); i++) {
            result.add(pq.poll()); // poll() возвращает минимальный элемент
        }
        return result;
    }

    // ─────────────────────────────────────────────
    // 3. Stream внутри цикла (вложенный стрим)
    //
    // ❌ Создаём новый стрим на каждой итерации внешнего цикла — O(n*m) накладных расходов.
    // ✅ flatMap объединяет в один проход.
    // ─────────────────────────────────────────────

    public List<Integer> badNestedStream(List<List<Integer>> matrix) {
        List<Integer> result = new ArrayList<>();
        for (List<Integer> row : matrix) {               // ❌ внешний цикл
            row.stream()                                 // ❌ новый стрим внутри цикла
               .filter(n -> n > 0)
               .forEach(result::add);
        }
        return result;
    }

    public List<Integer> goodFlatMap(List<List<Integer>> matrix) {
        return matrix.stream()
                .flatMap(Collection::stream)  // ✅ один стрим для всех строк
                .filter(n -> n > 0)
                .toList();
    }

    // ─────────────────────────────────────────────
    // 4. count() после collect() — лишняя аллокация
    //
    // ❌ Собираем весь список в память только чтобы посчитать размер.
    // ✅ count() — терминальная операция стрима, не создаёт коллекцию.
    // ─────────────────────────────────────────────

    public long badCount(List<String> items) {
        return items.stream()
                .filter(s -> s.startsWith("A"))
                .toList() // ❌ создаём список только для размера
                .size();
    }

    public long goodCount(List<String> items) {
        return items.stream()
                .filter(s -> s.startsWith("A"))
                .count(); // ✅ без промежуточной коллекции
    }

    // ─────────────────────────────────────────────
    // 5. Использование peek() для изменения состояния
    //
    // ❌ peek() предназначен для отладки, а не для side-effect логики.
    //    Может не выполниться если стрим оптимизирован (например, с limit()).
    // ✅ map() явно трансформирует элементы.
    // ─────────────────────────────────────────────

    public List<String> badPeekForTransform(List<String> items) {
        List<String> sideEffect = new ArrayList<>();
        items.stream()
             .peek(sideEffect::add) // ❌ side-effect в peek — непредсказуемо
             .toList();
        return sideEffect;
    }

    public List<String> goodMapForTransform(List<String> items) {
        return items.stream()
                .map(String::trim) // ✅ явная трансформация через map
                .toList();
    }

    // ─────────────────────────────────────────────
    // 6. anyMatch через collect + isEmpty (лишний проход)
    //
    // ❌ Собираем все совпадения чтобы проверить есть ли хоть одно.
    // ✅ anyMatch() останавливается на первом совпадении (short-circuit).
    // ─────────────────────────────────────────────

    public boolean badAnyMatch(List<Integer> numbers) {
        return !numbers.stream()
                .filter(n -> n > 100)
                .toList() // ❌ проходим весь список
                .isEmpty();
    }

    public boolean goodAnyMatch(List<Integer> numbers) {
        return numbers.stream()
                .anyMatch(n -> n > 100); // ✅ останавливается на первом > 100
    }

    // ─────────────────────────────────────────────
    // 7. Повторное создание стрима из одного источника
    //
    // ❌ Стрим нельзя использовать дважды — второй вызов бросает IllegalStateException.
    //    Обходят это пересозданием стрима — двойной проход по данным.
    // ✅ Собираем данные один раз, работаем с коллекцией.
    // ─────────────────────────────────────────────

    public Map<Boolean, Long> badDoubleStream(List<Integer> numbers) {
        long positiveCount = numbers.stream().filter(n -> n > 0).count(); // проход 1
        long negativeCount = numbers.stream().filter(n -> n < 0).count(); // проход 2 ❌
        Map<Boolean, Long> result = new HashMap<>();
        result.put(true, positiveCount);
        result.put(false, negativeCount);
        return result;
    }

    public Map<Boolean, Long> goodPartitioningBy(List<Integer> numbers) {
        return numbers.stream()
                .filter(n -> n != 0)
                .collect(Collectors.partitioningBy(
                        n -> n > 0,
                        Collectors.counting()  // ✅ один проход, два результата
                ));
    }

    // ─────────────────────────────────────────────
    // 8. toList() vs Collectors.toUnmodifiableList() — лишний враппер
    //
    // ❌ Collections.unmodifiableList(collect(toList())) — двойная обёртка.
    // ✅ Stream.toList() (Java 16+) возвращает неизменяемый список напрямую.
    // ─────────────────────────────────────────────

    public List<String> badUnmodifiable(List<String> items) {
        return Collections.unmodifiableList( // ❌ лишняя обёртка
                items.stream()
                     .filter(s -> !s.isEmpty())
                     .toList()
        );
    }

    public List<String> goodUnmodifiable(List<String> items) {
        return items.stream()
                .filter(s -> !s.isEmpty())
                .toList(); // ✅ Java 16+ — неизменяемый список сразу
    }
}
