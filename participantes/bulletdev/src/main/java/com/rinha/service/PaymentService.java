package com.rinha.service;

import com.rinha.client.PaymentProcessorClient;
import com.rinha.cache.CacheManager;
import com.rinha.model.PaymentRequest;
import com.rinha.model.PaymentResponse;
import com.rinha.model.PaymentSummary;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class PaymentService {
    private final PaymentProcessorClient client;
    private final ProcessorHealthService healthService;
    private final CacheManager cacheManager;

    public PaymentService(PaymentProcessorClient client, ProcessorHealthService healthService, CacheManager cacheManager) {
        this.client = client;
        this.healthService = healthService;
        this.cacheManager = cacheManager;
    }

    public CompletableFuture<PaymentResponse> processPayment(PaymentRequest request) {
        cacheManager.incrementTotalPayments();
        cacheManager.addToTotalAmount(request.getAmount());

        String primaryProcessor = "default";
        String fallbackProcessor = "fallback";

        if (!healthService.isProcessorHealthy(primaryProcessor)) {
            return processWithProcessor(request, fallbackProcessor);
        }

        return processWithProcessor(request, primaryProcessor)
                .exceptionally(throwable -> {
                    // Em caso de erro, tenta o fallback
                    return processWithProcessor(request, fallbackProcessor)
                            .exceptionally(fallbackThrowable -> {
                                // Se ambos falharem, retorna erro
                                cacheManager.incrementFailedPayments();
                                throw new RuntimeException("Both processors failed", fallbackThrowable);
                            })
                            .join();
                });
    }

    private CompletableFuture<PaymentResponse> processWithProcessor(PaymentRequest request, String processor) {
        return client.processPayment(request, processor)
                .thenApply(response -> {
                    cacheManager.incrementSuccessfulPayments();
                    if ("default".equals(processor)) {
                        cacheManager.incrementProcessorDefaultUsage();
                    } else {
                        cacheManager.incrementProcessorFallbackUsage();
                    }

                    response.setProcessor(processor);
                    return response;
                })
                .orTimeout(10, TimeUnit.SECONDS);
    }

    public PaymentSummary getPaymentSummary() {
        PaymentSummary cached = cacheManager.getSummary("current");
        if (cached != null) {
            return cached;
        }

        PaymentSummary summary = new PaymentSummary(
                cacheManager.getTotalPayments(),
                cacheManager.getTotalAmount(),
                cacheManager.getSuccessfulPayments(),
                cacheManager.getFailedPayments(),
                cacheManager.getProcessorDefaultUsage(),
                cacheManager.getProcessorFallbackUsage()
        );

        cacheManager.cacheSummary("current", summary);
        return summary;
    }
}