package com.highload.highload_presentation.io;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

public class FileReaderExample {

    /**
     * Чтение файла побайтово без буфера.
     * Каждый вызов read() — это системный вызов к ОС.
     */
    public long readWithoutBuffer(Path file) throws IOException {
        long totalBytes = 0;
        try (InputStream is = new FileInputStream(file.toFile())) {
            while (is.read() != -1) {
                totalBytes++;
            }
        }
        return totalBytes;
    }

    /**
     * Чтение файла с буфером.
     * BufferedInputStream читает блоками, минимизируя системные вызовы.
     */
    public long readWithBuffer(Path file) throws IOException {
        long totalBytes = 0;
        try (InputStream is = new BufferedInputStream(new FileInputStream(file.toFile()))) {
            while (is.read() != -1) {
                totalBytes++;
            }
        }
        return totalBytes;
    }
}
