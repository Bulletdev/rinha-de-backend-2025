package com.rinha.config;

public class AppConfig {
    private final int port;
    private final String processorDefaultUrl;
    private final String processorFallbackUrl;
    private final int connectionTimeout;
    private final int requestTimeout;
    private final int maxConnections;

    public AppConfig() {
        this.port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        this.processorDefaultUrl = System.getenv().getOrDefault("PROCESSOR_DEFAULT_URL", "http://processor-default:8080");
        this.processorFallbackUrl = System.getenv().getOrDefault("PROCESSOR_FALLBACK_URL", "http://processor-fallback:8080");
        this.connectionTimeout = Integer.parseInt(System.getenv().getOrDefault("CONNECTION_TIMEOUT", "5000"));
        this.requestTimeout = Integer.parseInt(System.getenv().getOrDefault("REQUEST_TIMEOUT", "10000"));
        this.maxConnections = Integer.parseInt(System.getenv().getOrDefault("MAX_CONNECTIONS", "100"));
    }

    public int getPort() { return port; }
    public String getProcessorDefaultUrl() { return processorDefaultUrl; }
    public String getProcessorFallbackUrl() { return processorFallbackUrl; }
    public int getConnectionTimeout() { return connectionTimeout; }
    public int getRequestTimeout() { return requestTimeout; }
    public int getMaxConnections() { return maxConnections; }
}