package com.rinha.service;

import com.rinha.client.PaymentProcessorClient;
import com.rinha.cache.CacheManager;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ProcessorHealthService {
    private final PaymentProcessorClient client;
    private final CacheManager cacheManager;
    private final ScheduledExecutorService scheduler;
    private final ConcurrentHashMap<String, Long> lastHealthCheck;
    private final long HEALTH_CHECK_INTERVAL = 5000;

    public ProcessorHealthService(PaymentProcessorClient client, CacheManager cacheManager) {
        this.client = client;
        this.cacheManager = cacheManager;
        this.scheduler = Executors.newScheduledThreadPool(2);
        this.lastHealthCheck = new ConcurrentHashMap<>();

        startPeriodicHealthChecks();
    }

    private void startPeriodicHealthChecks() {
        scheduler.scheduleAtFixedRate(() -> checkProcessorHealth("default"), 0, HEALTH_CHECK_INTERVAL, TimeUnit.MILLISECONDS);
        scheduler.scheduleAtFixedRate(() -> checkProcessorHealth("fallback"), 0, HEALTH_CHECK_INTERVAL, TimeUnit.MILLISECONDS);
    }

    public boolean isProcessorHealthy(String processor) {
        Boolean cached = cacheManager.getProcessorHealth(processor);
        if (cached != null) {
            return cached;
        }

        long now = System.currentTimeMillis();
        Long lastCheck = lastHealthCheck.get(processor);

        if (lastCheck == null || (now - lastCheck) >= HEALTH_CHECK_INTERVAL) {
            checkProcessorHealth(processor);
            lastHealthCheck.put(processor, now);
        }

        return cacheManager.getProcessorHealth(processor) != Boolean.FALSE;
    }

    private void checkProcessorHealth(String processor) {
        client.checkHealth(processor)
                .thenAccept(healthy -> cacheManager.cacheProcessorHealth(processor, healthy))
                .exceptionally(throwable -> {
                    cacheManager.cacheProcessorHealth(processor, false);
                    return null;
                });
    }

    public void shutdown() {
        scheduler.shutdown();
    }
}
