package com.highload.highload_presentation.io;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileReaderExampleTest {

    private final FileReaderExample reader = new FileReaderExample();

    @TempDir
    static Path tempDir;

    static Path testFile;
    static final int FILE_SIZE = 5 * 1024 * 1024; // 5 MB

    @BeforeAll
    static void setUp() throws IOException {
        testFile = tempDir.resolve("test-data.bin");
        byte[] data = new byte[FILE_SIZE];
        new Random(42).nextBytes(data);
        Files.write(testFile, data);
    }

    @Test
    void read_withoutBuffer_slow() throws IOException {
        long start = System.currentTimeMillis();
        long bytes = reader.readWithoutBuffer(testFile);
        long elapsed = System.currentTimeMillis() - start;

        assertEquals(FILE_SIZE, bytes);
        System.out.println("Without buffer: " + elapsed + " ms");
        assertTrue(elapsed >= 1000, "Expected at least 1s, got " + elapsed + " ms");
    }

    @Test
    void read_withBuffer_fast() throws IOException {
        long start = System.currentTimeMillis();
        long bytes = reader.readWithBuffer(testFile);
        long elapsed = System.currentTimeMillis() - start;

        assertEquals(FILE_SIZE, bytes);
        System.out.println("With buffer: " + elapsed + " ms");
        assertTrue(elapsed < 500, "Expected under 500ms, got " + elapsed + " ms");
    }
}
