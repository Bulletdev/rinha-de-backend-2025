package com.rinha.server;

import com.rinha.service.PaymentService;
import com.rinha.cache.CacheManager;
import com.rinha.handler.PaymentHandler;
import com.rinha.handler.SummaryHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;

import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class HttpRequestRouter extends SimpleChannelInboundHandler<FullHttpRequest> {
    private final PaymentHandler paymentHandler;
    private final SummaryHandler summaryHandler;

    public HttpRequestRouter(PaymentService paymentService, CacheManager cacheManager) {
        this.paymentHandler = new PaymentHandler(paymentService);
        this.summaryHandler = new SummaryHandler(paymentService, cacheManager);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) {
        String uri = request.uri();
        HttpMethod method = request.method();

        try {
            if (method == HttpMethod.POST && uri.equals("/payments")) {
                paymentHandler.handle(ctx, request);
            } else if (method == HttpMethod.GET && uri.equals("/payments-summary")) {
                summaryHandler.handle(ctx, request);
            } else {
                sendNotFound(ctx);
            }
        } catch (Exception e) {
            sendError(ctx, e);
        }
    }

    private void sendNotFound(ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(
                HTTP_1_1, NOT_FOUND, Unpooled.copiedBuffer("Not Found", CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain");
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        ctx.writeAndFlush(response);
    }

    private void sendError(ChannelHandlerContext ctx, Exception e) {
        FullHttpResponse response = new DefaultFullHttpResponse(
                HTTP_1_1, INTERNAL_SERVER_ERROR, Unpooled.copiedBuffer("Internal Server Error", CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain");
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        ctx.writeAndFlush(response);
    }
}