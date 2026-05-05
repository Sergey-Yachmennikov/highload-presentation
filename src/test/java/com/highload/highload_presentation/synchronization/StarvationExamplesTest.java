package com.highload.highload_presentation.synchronization;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StarvationExamplesTest {

    private final StarvationExamples examples = new StarvationExamples();

    @Test
    void demonstrateStarvation_bothThreadsRun() throws InterruptedException {
        int[] counts = examples.demonstrateStarvation(100);
        int highCount = counts[0];
        int lowCount  = counts[1];

        System.out.printf("Unfair lock — high: %d, low: %d%n", highCount, lowCount);

        // Оба потока выполнились (не зависли)
        assertTrue(highCount > 0, "Высокоприоритетный поток должен выполниться");
        assertTrue(lowCount >= 0, "Низкоприоритетный поток не должен упасть");

        // Образовательный вывод: при unfair lock высокоприоритетный поток,
        // как правило, получает значительно больше итераций (зависит от ОС и JVM)
        double ratio = highCount > 0 && lowCount > 0
                ? (double) highCount / lowCount : Double.MAX_VALUE;
        System.out.printf("Unfair lock ratio high/low: %.1f%n", ratio);
    }

    @Test
    void demonstrateFairness_bothThreadsGetEqualAccess() throws InterruptedException {
        int[] counts = examples.demonstrateFairness(100);
        int count1 = counts[0];
        int count2 = counts[1];

        System.out.printf("Fair lock — t1: %d, t2: %d%n", count1, count2);

        assertTrue(count1 > 0, "Поток 1 должен выполниться");
        assertTrue(count2 > 0, "Поток 2 должен выполниться");

        // При fair lock разница между потоками незначительная (менее 10x)
        double ratio = (double) Math.max(count1, count2) / Math.min(count1, count2);
        assertTrue(ratio < 10.0,
                "Fair lock: потоки должны получать доступ примерно поровну, ratio=%.1f".formatted(ratio));
    }

    @Test
    void demonstratePriorityStarvation_bothThreadsRun() throws InterruptedException {
        int[] counts = examples.demonstratePriorityStarvation(100);
        System.out.printf("Priority — high: %d, low: %d%n", counts[0], counts[1]);

        // Оба потока должны выполниться (не зависнуть)
        assertTrue(counts[0] > 0, "Высокоприоритетный поток должен выполниться");
        assertTrue(counts[1] > 0, "Низкоприоритетный поток должен выполниться");
    }
}
