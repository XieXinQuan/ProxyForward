package shop.huanting.server;

import com.alibaba.fastjson.JSON;
import entry.ModifyInfo;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * @author xie.xinquan
 * @create 2022/5/7 9:58
 * @company 深圳亿起融网络科技有限公司
 */
@Component
@Slf4j
public class ClientReceive implements InitializingBean, DisposableBean {

    @Value("${receive.server.port}")
    private int port;

    public ClientReceive() {
    }

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    private ChannelHandlerContext ctx;

    public synchronized void sendMsg(ModifyInfo msg) {
        if (ctx == null || ctx.isRemoved()) {
            return;
        }
        ctx.writeAndFlush(JSON.toJSONString(msg));
    }


    @Override
    public void afterPropertiesSet() {

        this.bossGroup = new NioEventLoopGroup();
        this.workerGroup = new NioEventLoopGroup();
        ServerBootstrap b = new ServerBootstrap();

        ClientReceive clientReceive = this;

        b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline().addLast(new StringDecoder(StandardCharsets.UTF_8));
                        ch.pipeline().addLast(new StringEncoder(StandardCharsets.UTF_8));
                        ch.pipeline().addLast(new SimpleHandler(clientReceive));
                    }
                });

        try {
            ChannelFuture f = b.bind(port).sync();
            //阻塞直到服务器关闭
//            f.channel().closeFuture().sync();
            log.info("Netty[{}] 启动成功", port);
        } catch (Exception e) {
            log.error("Netty 启动错误", e);
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    @Override
    public void destroy() {
        log.info("关闭 netty[{}] 服务", port);
        if (bossGroup.isShutdown()) {
            return;
        }
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
    }

    private void reset(ChannelHandlerContext ctx) {
        if (this.ctx != null && !this.ctx.isRemoved()) {
            this.ctx.disconnect();
        }
        this.ctx = ctx;
    }

    public static class SimpleHandler extends ChannelHandlerAdapter {

        private ClientReceive clientReceive;

        public SimpleHandler(ClientReceive clientReceive) {
            this.clientReceive = clientReceive;
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            super.channelActive(ctx);

            // 每次都放弃掉最旧的链接
            clientReceive.reset(ctx);

            log.info("有客户端链接");
            ModifyInfo modifyInfo = new ModifyInfo();
            modifyInfo.setType("ping");

            ChannelFuture future = ctx.writeAndFlush(JSON.toJSONString(modifyInfo));
            log.info("ping server ： done: {}, success: {}", future.isDone(), future.isSuccess());
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            String val ;
            if (msg instanceof String) {
                val = (String) msg;
            }else if (msg instanceof ByteBuf) {
                val = new String(Unpooled.copiedBuffer((ByteBuf) msg).array());
            }else {
                val = msg.toString();
            }
            log.debug("收到客户端消息 : {}", val);

            // 传递给前端
            WebSocketServer.sendMessage(val);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {

        }
    }
}
