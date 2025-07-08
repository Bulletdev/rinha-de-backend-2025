package com.rinha.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PaymentSummary {
    @JsonProperty("total_payments")
    private long totalPayments;

    @JsonProperty("total_amount")
    private long totalAmount;

    @JsonProperty("successful_payments")
    private long successfulPayments;

    @JsonProperty("failed_payments")
    private long failedPayments;

    @JsonProperty("processor_default_usage")
    private long processorDefaultUsage;

    @JsonProperty("processor_fallback_usage")
    private long processorFallbackUsage;

    public PaymentSummary() {}

    public PaymentSummary(long totalPayments, long totalAmount, long successfulPayments,
                          long failedPayments, long processorDefaultUsage, long processorFallbackUsage) {
        this.totalPayments = totalPayments;
        this.totalAmount = totalAmount;
        this.successfulPayments = successfulPayments;
        this.failedPayments = failedPayments;
        this.processorDefaultUsage = processorDefaultUsage;
        this.processorFallbackUsage = processorFallbackUsage;
    }

    public long getTotalPayments() { return totalPayments; }
    public void setTotalPayments(long totalPayments) { this.totalPayments = totalPayments; }

    public long getTotalAmount() { return totalAmount; }
    public void setTotalAmount(long totalAmount) { this.totalAmount = totalAmount; }

    public long getSuccessfulPayments() { return successfulPayments; }
    public void setSuccessfulPayments(long successfulPayments) { this.successfulPayments = successfulPayments; }

    public long getFailedPayments() { return failedPayments; }
    public void setFailedPayments(long failedPayments) { this.failedPayments = failedPayments; }

    public long getProcessorDefaultUsage() { return processorDefaultUsage; }
    public void setProcessorDefaultUsage(long processorDefaultUsage) { this.processorDefaultUsage = processorDefaultUsage; }

    public long getProcessorFallbackUsage() { return processorFallbackUsage; }
    public void setProcessorFallbackUsage(long processorFallbackUsage) { this.processorFallbackUsage = processorFallbackUsage; }
}
