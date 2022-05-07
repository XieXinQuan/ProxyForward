package shop.huanting.server;

import lombok.Getter;
import org.springframework.http.HttpHeaders;

/**
 * @author xie.xinquan
 * @create 2022/5/7 13:47
 * @company 深圳亿起融网络科技有限公司
 */
@Getter
public class SendData {

    private String type;

    private String method;

    private String path;

    private HttpHeaders httpHeaders;

    private String body;

    public SendData(String type, String method, String path, HttpHeaders httpHeaders, String body) {
        this.type = type;
        this.method = method;
        this.path = path;
        this.httpHeaders = httpHeaders;
        this.body = body;
    }
}
