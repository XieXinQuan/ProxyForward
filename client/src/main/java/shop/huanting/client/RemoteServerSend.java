package shop.huanting.client;

import com.alibaba.fastjson.JSON;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import reactor.netty.http.client.HttpClient;

import java.nio.charset.StandardCharsets;

/**
 * @author xie.xinquan
 * @create 2022/5/6 14:28
 * @company 深圳亿起融网络科技有限公司
 */
@Component
@Slf4j
@ChannelHandler.Sharable
public class RemoteServerSend extends ChannelInboundHandlerAdapter implements InitializingBean, DisposableBean {

    @Value("${custom.server.host}")
    private String serverHost;
    @Value("${custom.server.port}")
    private int serverPort;


    private ChannelHandlerContext ctx;
    private ChannelFuture future;

    private void againConnect() {
        if (ctx == null || ctx.isRemoved() || !ctx.channel().isActive()) {
            // 重连
            ctx = null;
            this.afterPropertiesSet();
        }
    }

    /**
     * 发消息只能同步发
     * @param method
     * @param path
     * @param httpHeaders
     * @param body
     */
    public synchronized void sendDataToServer(String method, String path, HttpHeaders httpHeaders, String body) {
        this.againConnect();
        if (ctx == null) {
            log.info("上报服务器错误，连接不上服务器");
            return;
        }

        SendData sendData = new SendData("request", method, path, httpHeaders, body);

        ctx.writeAndFlush(JSON.toJSONString(sendData));

    }

    @Override
    public void afterPropertiesSet() {
        log.info("开始连接服务器:  {}:{}", serverHost, serverPort);
        try {

            RemoteServerSend serverSend = this;

            // 首先，netty通过ServerBootstrap启动服务端
            Bootstrap client = new Bootstrap();

            //第1步 定义线程组，处理读写和链接事件，没有了accept事件
            EventLoopGroup group = new NioEventLoopGroup();
            client.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new ChannelInitializer<NioSocketChannel>() {  //通道是NioSocketChannel
                        @Override
                        protected void initChannel(NioSocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new StringEncoder(StandardCharsets.UTF_8));
                            ch.pipeline().addLast(new StringDecoder(StandardCharsets.UTF_8));
                            // 将自己添加到信道通知
                            ch.pipeline().addLast(serverSend);
                        }
                    });

            //连接服务器
            future = client.connect(serverHost, serverPort).sync();
        }catch (Exception e) {
            log.error("连接服务器错误：", e);
        }
    }

    @Override
    public void destroy() throws Exception {
        if (future != null && future.channel() != null) {
            future.channel().disconnect();
        }
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.info("连接服务器成功 : {}", ctx.channel().remoteAddress());

        String ping = JSON.toJSONString(new SendData("ping", null, null, null, null));
        ChannelFuture future = ctx.writeAndFlush(ping);
        log.info("send ping server ： done: {}, success: {}", future.isDone(), future.isSuccess());

        this.ctx = ctx;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        String val;
        if (msg instanceof String) {
            val = (String) msg;
        } else if (msg instanceof ByteBuf) {
            val = new String(Unpooled.copiedBuffer((ByteBuf) msg).array());
        } else {
            val = msg.toString();
        }
        log.info("收到服务器消息：" + val);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("与服务器通信错误：", cause);
        super.exceptionCaught(ctx, cause);
    }

}