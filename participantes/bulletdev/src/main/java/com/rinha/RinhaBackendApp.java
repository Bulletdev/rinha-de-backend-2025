package com.rinha;

import com.rinha.server.HttpServer;
import com.rinha.service.PaymentService;
import com.rinha.service.ProcessorHealthService;
import com.rinha.client.PaymentProcessorClient;
import com.rinha.cache.CacheManager;
import com.rinha.config.AppConfig;

public class RinhaBackendApp {
    public static void main(String[] args) throws Exception {
        AppConfig config = new AppConfig();

        CacheManager cacheManager = new CacheManager();
        PaymentProcessorClient processorClient = new PaymentProcessorClient(config);
        ProcessorHealthService healthService = new ProcessorHealthService(processorClient, cacheManager);
        PaymentService paymentService = new PaymentService(processorClient, healthService, cacheManager);

        HttpServer server = new HttpServer(config, paymentService, cacheManager);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down...");
            server.shutdown();
            processorClient.shutdown();
        }));

        server.start();
        System.out.println("Server started on port " + config.getPort());
    }
}