package com.rinha.handler;

import com.rinha.service.PaymentService;
import com.rinha.cache.CacheManager;
import com.rinha.model.PaymentSummary;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class SummaryHandler {
    private final PaymentService paymentService;
    private final CacheManager cacheManager;
    private final ObjectMapper objectMapper;

    public SummaryHandler(PaymentService paymentService, CacheManager cacheManager) {
        this.paymentService = paymentService;
        this.cacheManager = cacheManager;
        this.objectMapper = new ObjectMapper();
    }

    public void handle(ChannelHandlerContext ctx, FullHttpRequest request) {
        try {
            PaymentSummary summary = paymentService.getPaymentSummary();
            String jsonResponse = objectMapper.writeValueAsString(summary);

            FullHttpResponse response = new DefaultFullHttpResponse(
                    HTTP_1_1, OK, Unpooled.copiedBuffer(jsonResponse, CharsetUtil.UTF_8));
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json");
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());

            ctx.writeAndFlush(response);

        } catch (Exception e) {
            FullHttpResponse response = new DefaultFullHttpResponse(
                    HTTP_1_1, INTERNAL_SERVER_ERROR, Unpooled.copiedBuffer("Internal Server Error", CharsetUtil.UTF_8));
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain");
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
            ctx.writeAndFlush(response);
        }
    }
}
