package com.rinha.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PaymentResponse {
    @JsonProperty("payment_id")
    private String paymentId;

    @JsonProperty("status")
    private String status;

    @JsonProperty("processor")
    private String processor;

    public PaymentResponse() {}

    public PaymentResponse(String paymentId, String status, String processor) {
        this.paymentId = paymentId;
        this.status = status;
        this.processor = processor;
    }

    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getProcessor() { return processor; }
    public void setProcessor(String processor) { this.processor = processor; }
}