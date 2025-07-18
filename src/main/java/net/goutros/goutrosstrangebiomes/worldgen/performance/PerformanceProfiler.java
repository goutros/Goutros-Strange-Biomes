package net.goutros.goutrosstrangebiomes.worldgen.performance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Performance profiler for the blended terrain generation system
 */
public class PerformanceProfiler {

    private static final Logger LOGGER = LoggerFactory.getLogger("TerrainPerformance");
    private static final ConcurrentHashMap<String, AtomicLong> TIMING_DATA = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, AtomicInteger> CALL_COUNTS = new ConcurrentHashMap<>();
    private static final AtomicLong LAST_LOG_TIME = new AtomicLong(System.currentTimeMillis());

    private static final long LOG_INTERVAL_MS = 60000; // Log every minute

    public static void recordTiming(String operation, long timeNanos) {
        TIMING_DATA.computeIfAbsent(operation, k -> new AtomicLong(0)).addAndGet(timeNanos);
        CALL_COUNTS.computeIfAbsent(operation, k -> new AtomicInteger(0)).incrementAndGet();

        // Periodic logging
        long currentTime = System.currentTimeMillis();
        if (currentTime - LAST_LOG_TIME.get() > LOG_INTERVAL_MS) {
            if (LAST_LOG_TIME.compareAndSet(LAST_LOG_TIME.get(), currentTime)) {
                logPerformanceStats();
            }
        }
    }

    public static void logPerformanceStats() {
        if (TIMING_DATA.isEmpty()) return;

        LOGGER.info("=== Blended Terrain Generation Performance Stats ===");

        long totalTime = 0;
        int totalCalls = 0;

        for (String operation : TIMING_DATA.keySet()) {
            long opTotalTime = TIMING_DATA.get(operation).get();
            int opCallCount = CALL_COUNTS.get(operation).get();
            long avgTime = opCallCount > 0 ? opTotalTime / opCallCount : 0;

            totalTime += opTotalTime;
            totalCalls += opCallCount;

            LOGGER.info("{}: {} calls, avg {:.2f}ms, total {:.2f}ms",
                    operation, opCallCount, avgTime / 1_000_000.0, opTotalTime / 1_000_000.0);
        }

        if (totalCalls > 0) {
            LOGGER.info("TOTAL: {} calls, avg {:.2f}ms, total {:.2f}ms",
                    totalCalls, (totalTime / totalCalls) / 1_000_000.0, totalTime / 1_000_000.0);
        }

        LOGGER.info("=======================================================");

        // Reset counters after logging
        TIMING_DATA.clear();
        CALL_COUNTS.clear();
    }

    public static <T> T timeOperation(String operation, java.util.function.Supplier<T> supplier) {
        long start = System.nanoTime();
        try {
            return supplier.get();
        } finally {
            recordTiming(operation, System.nanoTime() - start);
        }
    }

    public static void timeOperation(String operation, Runnable runnable) {
        long start = System.nanoTime();
        try {
            runnable.run();
        } finally {
            recordTiming(operation, System.nanoTime() - start);
        }
    }
}