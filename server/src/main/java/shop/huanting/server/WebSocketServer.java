package shop.huanting.server;

import com.alibaba.fastjson.JSON;
import entry.SendData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;

/**
 * @author xie.xinquan
 * @create 2022/5/7 14:21
 * @company 深圳亿起融网络科技有限公司
 */
@ServerEndpoint("/websocket/{sid}")
@Component
@Slf4j
public class WebSocketServer {




    //concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。
    private static Set<WebSocketServer> webSocketSet = new CopyOnWriteArraySet<WebSocketServer>();

    //与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;

    //接收sid
    private Integer sid ;

    /**
     * 连接建立成功调用的方法*/
    @OnOpen
    public void onOpen(Session session, @PathParam("sid") Integer sid) {
        this.session = session;
        webSocketSet.add(this);     //加入set中
        this.sid = sid;
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        webSocketSet.remove(this);  //从set中删除
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息*/
    @OnMessage
    public void onMessage(String message, Session session) {
        log.info("收到来自窗口 {} 的消息 ： {}", sid, message);
    }

    /**
     *
     * @param session
     * @param error
     */
    @OnError
    public void onError(Session session, Throwable error) {
        log.error("发生错误", error);
    }

    private static StringBuilder cacheTxt = new StringBuilder();
    public static Map<String, SendData> pathDetailMap = new ConcurrentHashMap<>();

    private static ReceiveMessageTask thread = new ReceiveMessageTask();

    static {
        thread.start();
    }
    private static class ReceiveMessageTask extends Thread {

        private Queue<String> queue = new LinkedList<>();
        @Override
        public void run() {
            while (true) {
                while (!queue.isEmpty()) {
                    WebSocketServer.sendMessage(queue.poll());
                }
                try {
                    TimeUnit.MILLISECONDS.sleep(100);
                }catch (InterruptedException e) {}
            }
        }
    }

    public static void sendMessageAsync(String txt) {
        thread.queue.add(txt);
    }

    public static void sendMessage(String txt) {
        try {
            if (txt != null) {
                cacheTxt.append(txt);
            }

            int length = 0;
            int pos = 0;
            while (pos < cacheTxt.length() && cacheTxt.charAt(pos) != '{') {
                char c = cacheTxt.charAt(pos++);
                length = length * 10 + (c - '0');
            }

            // 是否结束一轮
            if (cacheTxt.length() == pos || cacheTxt.charAt(pos) != '{' || cacheTxt.length() - pos < length) {
                return;
            }

            String value = cacheTxt.substring(pos, pos + length);


            log.info("开始格式化数据：" + value);
            SendData sendData = JSON.parseObject(value, SendData.class);
            log.info("格式化成功");

            if ("ping".equals(sendData.getType())) {

            } else if ("request".equals(sendData.getType())) {
                pathDetailMap.put(sendData.getPath(), sendData);
                for (WebSocketServer socketServer : webSocketSet) {
                    socketServer.sendMessage(sendData.getMethod(), sendData.getPath());
                }
            }


            // 进入下次
            if (cacheTxt.length() - pos > length) {
                String substring = cacheTxt.substring(pos + length);
                cacheTxt = new StringBuilder(substring);
                // 触发读取
                sendMessage(null);
            }else {
                cacheTxt = new StringBuilder();
            }

        }catch (Exception e) {
            log.error("接收客户端信息处理异常", e);
        }

    }
    /**
     * 实现服务器主动推送
     */
    public void sendMessage(String method, String path) throws IOException {
        String txt = JSON.toJSONString(new SendData(null, method, path, null, null));
        this.session.getBasicRemote().sendText(txt);
    }
}
