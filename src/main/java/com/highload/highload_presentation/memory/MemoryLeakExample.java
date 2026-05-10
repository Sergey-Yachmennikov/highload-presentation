package com.highload.highload_presentation.memory;

import java.util.ArrayList;
import java.util.List;

public class MemoryLeakExample {

    private static final List<byte[]> leak = new ArrayList<>();

    /**
     * Заполняет heap блоками по 10 MB до OutOfMemoryError.
     * Запускать с флагами:
     * -Xmx128m -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=./heap.hprof
     */
    public void fillHeapUntilOom() {
        while (true) {
            leak.add(new byte[10 * 1024 * 1024]); // 10 MB
        }
    }
}
