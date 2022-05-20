package shop.huanting.client;

import com.alibaba.fastjson.JSON;
import entry.ModifyInfo;
import entry.SendData;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
    @Resource
    private ReceiveServer receiveServer;


    private ChannelHandlerContext ctx;
    private ChannelFuture future;



    private void againConnect() {
        if (ctx == null || ctx.isRemoved() || !ctx.channel().isActive()) {
            // 重连
            ctx = null;
            this.afterPropertiesSet();
        }
    }

    private SendTask sendTask = new SendTask();

    private static class SendTask extends Thread {

        private Lock lock = new ReentrantLock();
        private RemoteServerSend remoteServerSend;

        @Override
        public void run() {
            while (true) {
                while (!sendData.isEmpty()) {
                    SendData poll = sendData.poll();
                    if (poll == null) {
                        continue;
                    }
                    try {
                        String str = JSON.toJSONString(poll);
                        int length = str.length();
                        remoteServerSend.ctx.writeAndFlush(String.valueOf(length)).sync();
                        remoteServerSend.ctx.writeAndFlush(str).sync();
                    }catch (InterruptedException e) {}
                }
                try {
                    Thread.sleep(100);
                }catch (InterruptedException e) {}
            }
        }

        private Queue<SendData> sendData = new LinkedList<>();



    }

    /**
     * 发消息只能同步发
     * @param method
     * @param path
     * @param httpHeaders
     * @param body
     */
    public void sendDataToServer(String method, String path, HttpHeaders httpHeaders, String body) {
        this.againConnect();
        if (ctx == null) {
            log.info("上报服务器错误，连接不上服务器");
            return;
        }

        SendData sendData = new SendData("request", method, path, httpHeaders, body);
        sendTask.sendData.add(sendData);

    }

    @Override
    public void afterPropertiesSet() {
        log.info("开始连接服务器: {} : {}", serverHost, serverPort);
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
        log.info("启动后台发送线程");
        if (!sendTask.isAlive()) {
            sendTask.start();
            sendTask.remoteServerSend = this;
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
        int length = ping.length();
        ctx.writeAndFlush(String.valueOf(length));
        ChannelFuture future = ctx.writeAndFlush(ping);
        log.info("send ping server ： done: {}, success: {}", future.isDone(), future.isSuccess());

        this.ctx = ctx;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        String val = (String) msg;
        log.info("收到服务器消息：" + val);
        ModifyInfo modifyInfo = JSON.parseObject(val, ModifyInfo.class);
        if ("ping".equals(modifyInfo.getType())) {

        }else if ("addRule".equals(modifyInfo.getType())) {

            receiveServer.addRule(modifyInfo.getTargetPath(), modifyInfo);
        }else if ("removeRule".equals(modifyInfo.getType())) {

            receiveServer.removeRule(modifyInfo.getTargetPath());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("与服务器通信错误：", cause);
        super.exceptionCaught(ctx, cause);
    }

}
