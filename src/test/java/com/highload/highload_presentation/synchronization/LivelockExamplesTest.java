package com.highload.highload_presentation.synchronization;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LivelockExamplesTest {

    private final LivelockExamples examples = new LivelockExamples();

    @Test
    void demonstrateLivelock_exhaustsMaxAttempts() {
        // При livelock потоки вечно уступают друг другу — исчерпывают лимит попыток
        int maxAttempts = 50;
        int attempts = examples.demonstrateLivelock(maxAttempts);

        // Количество попыток близко к максимуму — прогресса не было
        assertEquals(maxAttempts, attempts,
                "Livelock: потоки должны исчерпать maxAttempts без прогресса");
    }

    @Test
    void resolveWithJitter_completesSuccessfully() throws InterruptedException {
        // Jitter разрывает симметрию — один из потоков всегда продвигается
        boolean result = examples.resolveWithJitter();
        assertTrue(result, "С jitter поток должен успешно захватить ресурс");
    }
}
