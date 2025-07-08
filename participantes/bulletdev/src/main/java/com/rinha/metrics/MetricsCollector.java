package com.rinha.metrics;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;


public class MetricsCollector {
    private final LongAdder totalRequests = new LongAdder();
    private final LongAdder successfulRequests = new LongAdder();
    private final LongAdder failedRequests = new LongAdder();
    private final AtomicLong averageLatency = new AtomicLong(0);

    private final AtomicLong[] latencyBuckets = new AtomicLong[10];

    public MetricsCollector() {
        for (int i = 0; i < latencyBuckets.length; i++) {
            latencyBuckets[i] = new AtomicLong(0);
        }
    }

    public void recordRequest(long latencyMs, boolean success) {
        totalRequests.increment();

        if (success) {
            successfulRequests.increment();
        } else {
            failedRequests.increment();
        }

        long current = averageLatency.get();
        long newAverage = (current + latencyMs) / 2;
        averageLatency.set(newAverage);

        int bucket = Math.min((int) (latencyMs / 10), latencyBuckets.length - 1);
        latencyBuckets[bucket].incrementAndGet();
    }

    public long getTotalRequests() { return totalRequests.sum(); }
    public long getSuccessfulRequests() { return successfulRequests.sum(); }
    public long getFailedRequests() { return failedRequests.sum(); }
    public long getAverageLatency() { return averageLatency.get(); }

    public double getSuccessRate() {
        long total = getTotalRequests();
        return total > 0 ? (double) getSuccessfulRequests() / total : 0.0;
    }
}