package com.rinha.handler;

import com.rinha.service.PaymentService;
import com.rinha.model.PaymentRequest;
import com.rinha.model.PaymentResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class PaymentHandler {
    private final PaymentService paymentService;
    private final ObjectMapper objectMapper;

    public PaymentHandler(PaymentService paymentService) {
        this.paymentService = paymentService;
        this.objectMapper = new ObjectMapper();
    }

    public void handle(ChannelHandlerContext ctx, FullHttpRequest request) {
        try {
            String body = request.content().toString(CharsetUtil.UTF_8);
            PaymentRequest paymentRequest = objectMapper.readValue(body, PaymentRequest.class);

            if (paymentRequest.getAmount() <= 0 || paymentRequest.getCurrency() == null || paymentRequest.getReference() == null) {
                sendBadRequest(ctx, "Invalid payment request");
                return;
            }

            paymentService.processPayment(paymentRequest)
                    .thenAccept(response -> {
                        try {
                            String jsonResponse = objectMapper.writeValueAsString(response);
                            sendJsonResponse(ctx, OK, jsonResponse);
                        } catch (Exception e) {
                            sendInternalError(ctx, "Failed to serialize response");
                        }
                    })
                    .exceptionally(throwable -> {
                        sendInternalError(ctx, "Payment processing failed");
                        return null;
                    });

        } catch (Exception e) {
            sendBadRequest(ctx, "Invalid JSON");
        }
    }

    private void sendJsonResponse(ChannelHandlerContext ctx, HttpResponseStatus status, String json) {
        FullHttpResponse response = new DefaultFullHttpResponse(
                HTTP_1_1, status, Unpooled.copiedBuffer(json, CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json");
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        ctx.writeAndFlush(response);
    }

    private void sendBadRequest(ChannelHandlerContext ctx, String message) {
        FullHttpResponse response = new DefaultFullHttpResponse(
                HTTP_1_1, BAD_REQUEST, Unpooled.copiedBuffer(message, CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain");
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        ctx.writeAndFlush(response);
    }

    private void sendInternalError(ChannelHandlerContext ctx, String message) {
        FullHttpResponse response = new DefaultFullHttpResponse(
                HTTP_1_1, INTERNAL_SERVER_ERROR, Unpooled.copiedBuffer(message, CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain");
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        ctx.writeAndFlush(response);
    }
}