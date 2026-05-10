package com.highload.highload_presentation.io;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AsyncFileReaderTest {

    private final AsyncFileReader reader = new AsyncFileReader();

    @TempDir
    static Path tempDir;

    static List<Path> testFiles;
    static final int FILE_COUNT = 10;
    static final int FILE_SIZE = 200 * 1024; // 200 KB each

    @BeforeAll
    static void setUp() throws IOException {
        testFiles = new ArrayList<>();
        Random random = new Random(42);
        for (int i = 0; i < FILE_COUNT; i++) {
            Path file = tempDir.resolve("file-" + i + ".bin");
            byte[] data = new byte[FILE_SIZE];
            random.nextBytes(data);
            Files.write(file, data);
            testFiles.add(file);
        }
    }

    @Test
    void readAll_sync_slow() throws IOException {
        long start = System.currentTimeMillis();
        long totalBytes = reader.readAllSync(testFiles);
        long elapsed = System.currentTimeMillis() - start;

        assertEquals((long) FILE_COUNT * FILE_SIZE, totalBytes);
        System.out.println("Sync: " + elapsed + " ms");
    }

    @Test
    void readAll_async_virtualThreads_fast() {
        long start = System.currentTimeMillis();
        long totalBytes = reader.readAllAsync(testFiles);
        long elapsed = System.currentTimeMillis() - start;

        assertEquals((long) FILE_COUNT * FILE_SIZE, totalBytes);
        System.out.println("Virtual Threads: " + elapsed + " ms");
        assertTrue(elapsed < 1000, "Expected under 1s, got " + elapsed + " ms");
    }

    @Test
    void readAll_nio_fast() {
        long start = System.currentTimeMillis();
        long totalBytes = reader.readAllNio(testFiles);
        long elapsed = System.currentTimeMillis() - start;

        assertEquals((long) FILE_COUNT * FILE_SIZE, totalBytes);
        System.out.println("NIO async: " + elapsed + " ms");
        assertTrue(elapsed < 1000, "Expected under 1s, got " + elapsed + " ms");
    }
}
