package shop.huanting.server;

import com.alibaba.fastjson.JSON;
import entry.SendData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

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

    public static void sendMessage(String txt) {
        try {
            cacheTxt.append(txt);
            // 一次发送完毕！！！
            String value = "[" + cacheTxt.toString() + "]";
            log.info("开始格式化数据：" + value);
            List<SendData> sendDataList = JSON.parseArray(value, SendData.class);
            log.info("格式化成功");

            for (SendData sendData : sendDataList) {
                if ("ping".equals(sendData.getType())) {

                } else if ("request".equals(sendData.getType())) {
                    for (WebSocketServer socketServer : webSocketSet) {
                        socketServer.sendMessage(sendData.getMethod(), sendData.getPath());
                    }
                }
            }

            // 重置
            cacheTxt = new StringBuilder();

        }catch (Exception e) {
            // 存在拆包情况，与 下一次结合
//            cacheTxt.append(txt);
            log.error("格式化json错误, 等待下一次解析");
            // 重置
//            cacheTxt = new StringBuilder();
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
