package net.justrotem.data.service;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class DataServiceShutdownController {

    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(4); // Adjust threads based on load

    private static final Set<CompletableFuture<?>> TRACKED_FUTURES =
            Collections.synchronizedSet(new HashSet<>());

    private static volatile boolean shuttingDown = false;

    private DataServiceShutdownController() {}

    /**
     * Execute a task asynchronously and track completion.
     */
    public static void executeAsync(Runnable task) {
        if (shuttingDown) return;

        CompletableFuture<Void> future = CompletableFuture.runAsync(task, EXECUTOR);
        track(future);
    }

    /**
     * Track an external CompletableFuture that also must finish on shutdown.
     */
    public static <T> CompletableFuture<T> track(CompletableFuture<T> future) {
        if (!shuttingDown) {
            TRACKED_FUTURES.add(future);
            future.whenComplete((r, e) -> TRACKED_FUTURES.remove(future));
        }
        return future;
    }

    /**
     * Called in onDisable() to gracefully finish outstanding tasks.
     */
    public static void shutdownAndAwait() {
        shuttingDown = true;

        EXECUTOR.shutdown(); // stop new async tasks
        waitForFutures();    // Wait for tracked futures to finish
        forceExecutorStop(); // If needed
    }

    private static void waitForFutures() {
        long timeout = System.currentTimeMillis() + 10000; // 10 seconds max wait

        while (!TRACKED_FUTURES.isEmpty() && System.currentTimeMillis() < timeout) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException ignored) {}
        }
    }

    private static void forceExecutorStop() {
        try {
            if (!EXECUTOR.awaitTermination(2, TimeUnit.SECONDS)) {
                EXECUTOR.shutdownNow(); // Hard shutdown fallback
            }
        } catch (InterruptedException e) {
            EXECUTOR.shutdownNow();
        }
    }
}