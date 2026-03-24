package net.justrotem.data.service;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;
import java.util.function.Supplier;

/**
 * Controls asynchronous data operations and ensures graceful shutdown.
 *
 * <p>This class provides:
 * <ul>
 *     <li>A shared executor for async data tasks</li>
 *     <li>Tracking of active {@link CompletableFuture} tasks</li>
 *     <li>Graceful shutdown logic to prevent data loss</li>
 * </ul>
 * </p>
 *
 * <p>Typical flow:
 * <ol>
 *     <li>Submit tasks using {@link #executeAsync(Runnable)} or {@link #supplyAsync(Supplier)}</li>
 *     <li>Tasks are automatically tracked</li>
 *     <li>On shutdown, call {@link #shutdownAndAwait()}</li>
 *     <li>The system waits for tasks to finish before closing</li>
 * </ol>
 * </p>
 *
 * <p>This class is thread-safe.</p>
 */
public final class DataServiceShutdownController {

    /**
     * Shared executor for all async data operations.
     */
    private static final ExecutorService EXECUTOR =
            Executors.newFixedThreadPool(4);

    /**
     * Tracks all active async operations.
     */
    private static final Set<CompletableFuture<?>> TRACKED_FUTURES =
            Collections.synchronizedSet(new HashSet<>());

    /**
     * Indicates whether the system is shutting down.
     *
     * <p>When true, new tasks will not be accepted.</p>
     */
    private static volatile boolean shuttingDown = false;

    private DataServiceShutdownController() {
    }

    /**
     * Executes a task asynchronously and tracks its completion.
     *
     * <p>If shutdown has started, the task will not be executed.</p>
     *
     * @param task the task to execute
     */
    public static void executeAsync(Runnable task) {
        if (shuttingDown) return;

        CompletableFuture<Void> future = CompletableFuture.runAsync(task, EXECUTOR);
        track(future);
    }

    /**
     * Executes a supplier asynchronously and returns a tracked future.
     *
     * <p>If shutdown has started, the returned future will fail immediately.</p>
     *
     * @param supplier the supplier to execute
     * @param <T>      result type
     * @return tracked CompletableFuture
     */
    public static <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier) {
        if (shuttingDown) {
            return CompletableFuture.failedFuture(
                    new IllegalStateException("Data service is shutting down.")
            );
        }

        CompletableFuture<T> future = CompletableFuture.supplyAsync(supplier, EXECUTOR);
        return track(future);
    }

    /**
     * Tracks an existing CompletableFuture for shutdown handling.
     *
     * <p>This ensures that externally created async operations are also awaited
     * during shutdown.</p>
     *
     * @param future the future to track
     * @param <T>    result type
     * @return the same future instance
     */
    public static <T> CompletableFuture<T> track(CompletableFuture<T> future) {
        if (!shuttingDown) {
            TRACKED_FUTURES.add(future);
            future.whenComplete((r, e) -> TRACKED_FUTURES.remove(future));
        }
        return future;
    }

    /**
     * Initiates shutdown and waits for all tracked tasks to complete.
     *
     * <p>This method should be called during application shutdown (e.g. plugin disable).</p>
     *
     * <p>Shutdown steps:
     * <ol>
     *     <li>Stop accepting new tasks</li>
     *     <li>Wait for tracked futures (up to timeout)</li>
     *     <li>Force shutdown if necessary</li>
     * </ol>
     * </p>
     */
    public static void shutdownAndAwait() {
        shuttingDown = true;

        EXECUTOR.shutdown();   // Stop new tasks
        waitForFutures();      // Wait for tracked futures
        forceExecutorStop();   // Force stop if needed
    }

    /**
     * Waits for tracked futures to complete.
     *
     * <p>This uses a simple polling loop with a timeout to avoid blocking indefinitely.</p>
     */
    private static void waitForFutures() {
        long timeout = System.currentTimeMillis() + 10000; // 10 seconds max

        while (!TRACKED_FUTURES.isEmpty() && System.currentTimeMillis() < timeout) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException ignored) {
            }
        }
    }

    /**
     * Forces executor shutdown if tasks did not finish in time.
     */
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