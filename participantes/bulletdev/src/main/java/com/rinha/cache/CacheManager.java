package com.rinha.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.rinha.model.PaymentSummary;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

public class CacheManager {
    private final Cache<String, PaymentSummary> summaryCache;

    private final Cache<String, Boolean> processorHealthCache;

    private final AtomicLong totalPayments = new AtomicLong(0);
    private final AtomicLong totalAmount = new AtomicLong(0);
    private final AtomicLong successfulPayments = new AtomicLong(0);
    private final AtomicLong failedPayments = new AtomicLong(0);
    private final AtomicLong processorDefaultUsage = new AtomicLong(0);
    private final AtomicLong processorFallbackUsage = new AtomicLong(0);

    public CacheManager() {
        this.summaryCache = Caffeine.newBuilder()
                .maximumSize(100)
                .expireAfterWrite(Duration.ofSeconds(10))
                .build();

        this.processorHealthCache = Caffeine.newBuilder()
                .maximumSize(10)
                .expireAfterWrite(Duration.ofSeconds(30))
                .build();
    }

    public PaymentSummary getSummary(String key) {
        return summaryCache.getIfPresent(key);
    }

    public void cacheSummary(String key, PaymentSummary summary) {
        summaryCache.put(key, summary);
    }

    public Boolean getProcessorHealth(String processor) {
        return processorHealthCache.getIfPresent(processor);
    }

    public void cacheProcessorHealth(String processor, boolean healthy) {
        processorHealthCache.put(processor, healthy);
    }

    public void incrementTotalPayments() {
        totalPayments.incrementAndGet();
    }

    public void addToTotalAmount(long amount) {
        totalAmount.addAndGet(amount);
    }

    public void incrementSuccessfulPayments() {
        successfulPayments.incrementAndGet();
    }

    public void incrementFailedPayments() {
        failedPayments.incrementAndGet();
    }

    public void incrementProcessorDefaultUsage() {
        processorDefaultUsage.incrementAndGet();
    }

    public void incrementProcessorFallbackUsage() {
        processorFallbackUsage.incrementAndGet();
    }

    public long getTotalPayments() { return totalPayments.get(); }
    public long getTotalAmount() { return totalAmount.get(); }
    public long getSuccessfulPayments() { return successfulPayments.get(); }
    public long getFailedPayments() { return failedPayments.get(); }
    public long getProcessorDefaultUsage() { return processorDefaultUsage.get(); }
    public long getProcessorFallbackUsage() { return processorFallbackUsage.get(); }
}