package com.rinha.client;

import com.rinha.config.AppConfig;
import com.rinha.model.PaymentRequest;
import com.rinha.model.PaymentResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.asynchttpclient.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class PaymentProcessorClient {
    private final AsyncHttpClient httpClient;
    private final AppConfig config;
    private final ObjectMapper objectMapper;

    public PaymentProcessorClient(AppConfig config) {
        this.config = config;
        this.objectMapper = new ObjectMapper();

        DefaultAsyncHttpClientConfig.Builder clientConfig = new DefaultAsyncHttpClientConfig.Builder()
                .setConnectTimeout(config.getConnectionTimeout())
                .setRequestTimeout(config.getRequestTimeout())
                .setMaxConnections(config.getMaxConnections())
                .setMaxConnectionsPerHost(config.getMaxConnections() / 2)
                .setKeepAlive(true)
                .setPooledConnectionIdleTimeout(30000)
                .setConnectionTtl(60000)
                .setUseOpenSsl(false); // Usar JDK SSL para melhor performance em containers

        this.httpClient = Dsl.asyncHttpClient(clientConfig.build());
    }

    public CompletableFuture<PaymentResponse> processPayment(PaymentRequest request, String processor) {
        String url = getProcessorUrl(processor) + "/payments";

        try {
            String jsonBody = objectMapper.writeValueAsString(request);

            Request httpRequest = Dsl.post(url)
                    .setBody(jsonBody)
                    .setHeader("Content-Type", "application/json")
                    .build();

            return httpClient.executeRequest(httpRequest)
                    .toCompletableFuture()
                    .thenApply(response -> {
                        if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                            try {
                                return objectMapper.readValue(response.getResponseBody(), PaymentResponse.class);
                            } catch (Exception e) {
                                throw new RuntimeException("Failed to parse response", e);
                            }
                        } else {
                            throw new RuntimeException("HTTP " + response.getStatusCode() + ": " + response.getResponseBody());
                        }
                    })
                    .orTimeout(config.getRequestTimeout(), TimeUnit.MILLISECONDS);

        } catch (Exception e) {
            CompletableFuture<PaymentResponse> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }

    public CompletableFuture<Boolean> checkHealth(String processor) {
        String url = getProcessorUrl(processor) + "/health";

        Request request = Dsl.get(url)
                .setHeader("Accept", "application/json")
                .build();

        return httpClient.executeRequest(request)
                .toCompletableFuture()
                .thenApply(response -> response.getStatusCode() == 200)
                .exceptionally(throwable -> false)
                .orTimeout(5000, TimeUnit.MILLISECONDS)
                .exceptionally(throwable -> false);
    }

    private String getProcessorUrl(String processor) {
        return "default".equals(processor) ? config.getProcessorDefaultUrl() : config.getProcessorFallbackUrl();
    }

    public void shutdown() {
        try {
            httpClient.close();
        } catch (Exception e) {
        }
    }
}