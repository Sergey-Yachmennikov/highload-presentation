package com.highload.highload_presentation.memory;


public class LoopInitialization {

    /**
     * Неправильно: создаём тяжёлый объект внутри каждой итерации,
     * хотя его можно переиспользовать.
     */
    public long processWithReallocation(int iterations) {
        long sum = 0;
        for (int i = 0; i < iterations; i++) {
            int[] buffer = new int[10_000];
            buffer[0] = i;
            sum += buffer[0];
        }
        return sum;
    }

    /**
     * Правильно: переиспользуем буфер, одна аллокация вместо миллионов.
     */
    public long processWithReuse(int iterations) {
        long sum = 0;
        int[] buffer = new int[10_000];
        for (int i = 0; i < iterations; i++) {
            buffer[0] = i;
            sum += buffer[0];
        }
        return sum;
    }
}
