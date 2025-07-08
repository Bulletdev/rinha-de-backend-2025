package com.rinha.server;

import com.rinha.config.AppConfig;
import com.rinha.service.PaymentService;
import com.rinha.cache.CacheManager;
import com.rinha.handler.PaymentHandler;
import com.rinha.handler.SummaryHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.util.concurrent.DefaultThreadFactory;

public class HttpServer {
    private final AppConfig config;
    private final PaymentService paymentService;
    private final CacheManager cacheManager;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel serverChannel;

    public HttpServer(AppConfig config, PaymentService paymentService, CacheManager cacheManager) {
        this.config = config;
        this.paymentService = paymentService;
        this.cacheManager = cacheManager;
    }

    public void start() throws InterruptedException {
        // Otimização: usar threads com nomes específicos
        bossGroup = new NioEventLoopGroup(1, new DefaultThreadFactory("netty-boss"));
        workerGroup = new NioEventLoopGroup(0, new DefaultThreadFactory("netty-worker"));

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new HttpRequestDecoder());
                            pipeline.addLast(new HttpResponseEncoder());
                            pipeline.addLast(new HttpObjectAggregator(65536)); // 64KB max
                            pipeline.addLast(new HttpRequestRouter(paymentService, cacheManager));
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.SO_REUSEADDR, true);

            ChannelFuture future = bootstrap.bind(config.getPort()).sync();
            serverChannel = future.channel();

        } catch (Exception e) {
            shutdown();
            throw e;
        }
    }

    public void shutdown() {
        if (serverChannel != null) {
            serverChannel.close().awaitUninterruptibly();
        }
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
    }
}
