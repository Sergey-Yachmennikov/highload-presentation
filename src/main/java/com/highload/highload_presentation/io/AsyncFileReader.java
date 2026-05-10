package com.highload.highload_presentation.io;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class AsyncFileReader {

    /**
     * Синхронное последовательное чтение нескольких файлов.
     * Каждый файл читается только после завершения предыдущего.
     */
    public long readAllSync(List<Path> files) throws IOException {
        long totalBytes = 0;
        for (Path file : files) {
            totalBytes += readFile(file);
        }
        return totalBytes;
    }

    /**
     * Параллельное чтение нескольких файлов через Virtual Threads.
     * Каждый файл читается в своём виртуальном потоке.
     * При блокировке на I/O виртуальный поток освобождает carrier thread.
     */
    public long readAllAsync(List<Path> files) {
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<Long>> futures = files.stream()
                    .map(file -> executor.submit(() -> readFile(file)))
                    .toList();

            return futures.stream()
                    .mapToLong(f -> {
                        try {
                            return f.get();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .sum();
        }
    }

    private long readFile(Path file) throws IOException {
        byte[] buffer = new byte[4096];
        long totalBytes = 0;
        try (var is = Files.newInputStream(file)) {
            int read;
            while ((read = is.read(buffer)) != -1) {
                totalBytes += read;
                simulateProcessing();
            }
        }
        return totalBytes;
    }

    /**
     * Асинхронное параллельное чтение через NIO AsynchronousFileChannel.
     * Использует callback'и (CompletionHandler) — не блокирует потоки вообще.
     */
    public long readAllNio(List<Path> files) {
        List<CompletableFuture<Long>> futures = files.stream()
                .map(this::readFileNio)
                .toList();

        return futures.stream()
                .mapToLong(CompletableFuture::join)
                .sum();
    }

    private CompletableFuture<Long> readFileNio(Path file) {
        CompletableFuture<Long> future = new CompletableFuture<>();
        try {
            AsynchronousFileChannel channel = AsynchronousFileChannel.open(file, StandardOpenOption.READ);
            ByteBuffer buffer = ByteBuffer.allocate(4096);
            readChunk(channel, buffer, 0, 0L, future);
        } catch (IOException e) {
            future.completeExceptionally(e);
        }
        return future;
    }

    private void readChunk(AsynchronousFileChannel channel, ByteBuffer buffer,
                           long position, long accumulated, CompletableFuture<Long> future) {
        channel.read(buffer, position, null, new CompletionHandler<Integer, Void>() {
            @Override
            public void completed(Integer bytesRead, Void attachment) {
                if (bytesRead == -1) {
                    closeChannel(channel);
                    future.complete(accumulated);
                    return;
                }
                buffer.clear();
                simulateProcessing();
                readChunk(channel, buffer, position + bytesRead, accumulated + bytesRead, future);
            }

            @Override
            public void failed(Throwable exc, Void attachment) {
                closeChannel(channel);
                future.completeExceptionally(exc);
            }
        });
    }

    private void closeChannel(AsynchronousFileChannel channel) {
        try {
            channel.close();
        } catch (IOException ignored) {
        }
    }

    /**
     * Имитация обработки прочитанных данных (парсинг, валидация и т.д.)
     */
    private static void simulateProcessing() {
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
