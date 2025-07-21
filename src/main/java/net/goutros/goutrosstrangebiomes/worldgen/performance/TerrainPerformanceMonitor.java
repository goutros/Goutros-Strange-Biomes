package net.goutros.goutrosstrangebiomes.worldgen.performance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * TERRAIN PERFORMANCE MONITOR
 *
 * Tracks performance metrics for the enhanced terrain generation system
 * to help optimize and debug the terrain resculpting process.
 */
public class TerrainPerformanceMonitor {
    private static final Logger LOGGER = LoggerFactory.getLogger("TerrainPerformance");

    // Performance tracking
    private static final AtomicLong totalProcessingTime = new AtomicLong(0);
    private static final AtomicInteger chunksProcessed = new AtomicInteger(0);
    private static final AtomicInteger chunksSkipped = new AtomicInteger(0);
    private static final AtomicLong cacheHits = new AtomicLong(0);
    private static final AtomicLong cacheMisses = new AtomicLong(0);

    // Timing data for different operations
    private static final ConcurrentHashMap<String, AtomicLong> operationTimes = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, AtomicInteger> operationCounts = new ConcurrentHashMap<>();

    // Configuration
    private static final long REPORT_INTERVAL_MS = 60000; // Report every minute
    private static AtomicLong lastReportTime = new AtomicLong(System.currentTimeMillis());

    /**
     * Record timing for a terrain operation
     */
    public static void recordOperation(String operation, long timeNanos) {
        operationTimes.computeIfAbsent(operation, k -> new AtomicLong(0)).addAndGet(timeNanos);
        operationCounts.computeIfAbsent(operation, k -> new AtomicInteger(0)).incrementAndGet();

        // Check if we should report statistics
        checkAndReportStats();
    }

    /**
     * Record chunk processing time
     */
    public static void recordChunkProcessing(long timeNanos, boolean skipped) {
        if (skipped) {
            chunksSkipped.incrementAndGet();
        } else {
            chunksProcessed.incrementAndGet();
            totalProcessingTime.addAndGet(timeNanos);
        }
    }

    /**
     * Record cache performance
     */
    public static void recordCacheHit() {
        cacheHits.incrementAndGet();
    }

    public static void recordCacheMiss() {
        cacheMisses.incrementAndGet();
    }

    /**
     * Time an operation and record the result
     */
    public static <T> T timeOperation(String operation, java.util.function.Supplier<T> supplier) {
        long start = System.nanoTime();
        try {
            return supplier.get();
        } finally {
            recordOperation(operation, System.nanoTime() - start);
        }
    }

    /**
     * Time a void operation and record the result
     */
    public static void timeOperation(String operation, Runnable runnable) {
        long start = System.nanoTime();
        try {
            runnable.run();
        } finally {
            recordOperation(operation, System.nanoTime() - start);
        }
    }

    /**
     * Check if we should report stats and do so if needed
     */
    private static void checkAndReportStats() {
        long currentTime = System.currentTimeMillis();
        long lastReport = lastReportTime.get();

        if (currentTime - lastReport > REPORT_INTERVAL_MS) {
            if (lastReportTime.compareAndSet(lastReport, currentTime)) {
                reportPerformanceStats();
            }
        }
    }

    /**
     * Generate and log performance statistics
     */
    public static void reportPerformanceStats() {
        int processed = chunksProcessed.get();
        int skipped = chunksSkipped.get();
        long totalTime = totalProcessingTime.get();

        if (processed == 0 && skipped == 0) {
            return; // No data to report
        }

        LOGGER.info("=== TERRAIN GENERATION PERFORMANCE REPORT ===");

        // Chunk processing stats
        LOGGER.info("Chunks processed: {}, skipped: {}, total: {}",
                processed, skipped, processed + skipped);

        if (processed > 0) {
            double avgTimeMs = (totalTime / processed) / 1_000_000.0;
            LOGGER.info("Average processing time per chunk: {:.2f}ms", avgTimeMs);

            double chunksPerSecond = processed / ((totalTime / 1_000_000_000.0));
            LOGGER.info("Processing rate: {:.1f} chunks/second", chunksPerSecond);
        }

        // Cache performance
        long hits = cacheHits.get();
        long misses = cacheMisses.get();
        if (hits + misses > 0) {
            double hitRate = (hits * 100.0) / (hits + misses);
            LOGGER.info("Cache hit rate: {:.1f}% ({} hits, {} misses)", hitRate, hits, misses);
        }

        // Operation breakdown
        if (!operationTimes.isEmpty()) {
            LOGGER.info("--- Operation Breakdown ---");
            operationTimes.forEach((operation, totalTimeAtomic) -> {
                long opTotalTime = totalTimeAtomic.get();
                int opCount = operationCounts.get(operation).get();

                if (opCount > 0) {
                    double avgOpTime = (opTotalTime / opCount) / 1_000_000.0;
                    double percentOfTotal = (opTotalTime * 100.0) / totalTime;

                    LOGGER.info("{}: {} calls, avg {:.2f}ms, {:.1f}% of total time",
                            operation, opCount, avgOpTime, percentOfTotal);
                }
            });
        }

        LOGGER.info("================================================");

        // Reset counters for next reporting period
        resetCounters();
    }

    /**
     * Reset performance counters
     */
    private static void resetCounters() {
        totalProcessingTime.set(0);
        chunksProcessed.set(0);
        chunksSkipped.set(0);
        cacheHits.set(0);
        cacheMisses.set(0);
        operationTimes.clear();
        operationCounts.clear();
    }

    /**
     * Get current performance summary as string
     */
    public static String getPerformanceSummary() {
        int processed = chunksProcessed.get();
        int skipped = chunksSkipped.get();
        long totalTime = totalProcessingTime.get();

        if (processed == 0) {
            return "No chunks processed yet";
        }

        double avgTimeMs = (totalTime / processed) / 1_000_000.0;
        long hits = cacheHits.get();
        long misses = cacheMisses.get();
        double hitRate = (hits + misses > 0) ? (hits * 100.0) / (hits + misses) : 0.0;

        return String.format("Processed: %d chunks, Avg: %.2fms/chunk, Cache: %.1f%% hit rate",
                processed, avgTimeMs, hitRate);
    }

    /**
     * Force immediate performance report
     */
    public static void forceReport() {
        reportPerformanceStats();
    }

    /**
     * Check if terrain generation is performing well
     */
    public static boolean isPerformingWell() {
        int processed = chunksProcessed.get();
        if (processed < 10) return true; // Not enough data

        long totalTime = totalProcessingTime.get();
        double avgTimeMs = (totalTime / processed) / 1_000_000.0;

        // Consider performance good if average chunk processing is under 5ms
        return avgTimeMs < 5.0;
    }

    /**
     * Get detailed performance metrics for debugging
     */
    public static String getDetailedMetrics() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== DETAILED TERRAIN METRICS ===\n");

        sb.append(String.format("Chunks: %d processed, %d skipped\n",
                chunksProcessed.get(), chunksSkipped.get()));

        long totalTime = totalProcessingTime.get();
        if (chunksProcessed.get() > 0) {
            double avgTime = (totalTime / chunksProcessed.get()) / 1_000_000.0;
            sb.append(String.format("Average processing time: %.2fms\n", avgTime));
        }

        long hits = cacheHits.get();
        long misses = cacheMisses.get();
        if (hits + misses > 0) {
            double hitRate = (hits * 100.0) / (hits + misses);
            sb.append(String.format("Cache performance: %.1f%% hit rate\n", hitRate));
        }

        if (!operationTimes.isEmpty()) {
            sb.append("Operation times:\n");
            operationTimes.forEach((op, time) -> {
                int count = operationCounts.get(op).get();
                if (count > 0) {
                    double avgOpTime = (time.get() / count) / 1_000_000.0;
                    sb.append(String.format("  %s: %.2fms avg (%d calls)\n", op, avgOpTime, count));
                }
            });
        }

        return sb.toString();
    }
}