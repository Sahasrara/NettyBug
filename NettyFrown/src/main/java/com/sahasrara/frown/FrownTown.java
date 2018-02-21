package com.sahasrara.frown;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.Channel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * Steps to reproduce the issue.
 *  - run: $nc -lk localhost 9999
 *  - Run main.
 */
public class FrownTown {

    public static void main(String[] args) throws InterruptedException {
        // Simulate Background Noise
        BackgroundNoise.makeSomeNoise();

        // Initialize the culprit
        NioEventLoopGroup sinner = new NioEventLoopGroup(5);

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(sinner);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
            bootstrap.handler(new ChannelInitializer<Channel>() {
                protected void initChannel(Channel channel) throws Exception {
                    channel
                            .pipeline()
                            .addLast(new IdleStateHandler(0, 0, 30))
                            .addLast(new DemoHandler());
                }
            });
            long startTime = System.currentTimeMillis();

            // Send some messages
            for (int i = 0; i < 10; i++) {
                try {
                    sendMessage(bootstrap);
                } catch (Exception e) {
                    System.out.println("Sending failed to send message " + e.getMessage());
                }
            }
            long secsTook = (System.currentTimeMillis() - startTime) / 1000;
            System.out.println("Hopefully this took at least 30 seconds longer than you might expected.  Took "
                    + secsTook + " seconds");
        } catch (Exception e) {
            System.out.println("Something dumb happened " + e.getMessage());
        } finally {
            sinner.shutdownGracefully();
        }
    }

    private static void sendMessage(Bootstrap bootstrap) {
        System.out.println("Sending a new garbage message");

        // Start the client.
        ChannelFuture connectionFuture = bootstrap
                .connect("localhost", 9999)
                .syncUninterruptibly();

        // Fire off a request
        if (connectionFuture.isSuccess()) {
            ByteBuf message = UnpooledByteBufAllocator.DEFAULT.buffer(6);
            message.writeBytes("Hello\n".getBytes());
            connectionFuture
                    .channel()
                    .write(message)
                    .addListener(ChannelFutureListener.CLOSE)
                    .syncUninterruptibly();
        } else {
            System.out.println("Failed to connect.");
            connectionFuture.channel().close();
        }
    }
}
