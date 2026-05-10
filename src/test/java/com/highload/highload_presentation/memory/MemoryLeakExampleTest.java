package com.highload.highload_presentation.memory;

import org.junit.jupiter.api.Test;

/**
 * Запускать с VM options:
 * -Xmx256m -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=$PROJECT_DIR$/heap.hprof
 *
 * После падения открыть heap.hprof в IntelliJ: File -> Open -> heap.hprof
 * Там будет видно что byte[] массивы занимают всю память.
 *
 * Тест намеренно НЕ перехватывает OOM — иначе heap dump не создастся.
 */
class MemoryLeakExampleTest {

    @Test
    void fillHeap_causesOutOfMemoryError() {
        MemoryLeakExample example = new MemoryLeakExample();
        example.fillHeapUntilOom();
    }
}
