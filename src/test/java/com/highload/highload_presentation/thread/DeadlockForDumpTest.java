package com.highload.highload_presentation.thread;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeadlockForDumpTest {

    @Test
    void startDeadlock_detectedByThreadMXBean() throws InterruptedException {
        DeadlockForDump example = new DeadlockForDump();

        Thread deadlockThread = Thread.ofPlatform().name("deadlock-launcher").start(() -> {
            try {
                example.startDeadlock();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        // Даём время потокам войти в deadlock
        Thread.sleep(2000);

        // Программная детекция deadlock через ThreadMXBean
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        long[] deadlockedThreads = threadMXBean.findDeadlockedThreads();

        assertNotNull(deadlockedThreads, "Deadlock должен быть обнаружен");
        assertTrue(deadlockedThreads.length >= 2, "Минимум 2 потока в deadlock");

        System.out.println("Обнаружен deadlock! Потоки:");
        for (long threadId : deadlockedThreads) {
            System.out.println("  - " + threadMXBean.getThreadInfo(threadId).getThreadName());
        }

        // Прерываем для завершения теста
        deadlockThread.interrupt();
    }

    @Test
    void startDeadlock_detectedByJstack() throws Exception {
        DeadlockForDump example = new DeadlockForDump();

        Thread deadlockThread = Thread.ofPlatform().name("deadlock-launcher").start(() -> {
            try {
                example.startDeadlock();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        // Даём время потокам войти в deadlock
        Thread.sleep(2000);

        // Снимаем thread dump через jstack
        long pid = ProcessHandle.current().pid();
        System.out.println("PID: " + pid);

        Process jstack = new ProcessBuilder("jstack", String.valueOf(pid))
                .redirectErrorStream(true)
                .start();

        String threadDump;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(jstack.getInputStream()))) {
            threadDump = reader.lines().collect(Collectors.joining("\n"));
        }
        jstack.waitFor();

        System.out.println("=== THREAD DUMP (фрагмент) ===");
        // Выводим только секцию с deadlock
        int deadlockIndex = threadDump.indexOf("Found one Java-level deadlock");
        if (deadlockIndex != -1) {
            System.out.println(threadDump.substring(deadlockIndex));
        } else {
            System.out.println(threadDump);
        }

        assertTrue(threadDump.contains("Found one Java-level deadlock"),
                "jstack должен обнаружить deadlock");
        assertTrue(threadDump.contains("worker-A-then-B"));
        assertTrue(threadDump.contains("worker-B-then-A"));

        deadlockThread.interrupt();
    }
}
