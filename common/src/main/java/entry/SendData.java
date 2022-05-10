package entry;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpHeaders;

/**
 * @author xie.xinquan
 * @create 2022/5/9 16:45
 * @company 深圳亿起融网络科技有限公司
 */
@Getter
@AllArgsConstructor
public class SendData {

    private String type;

    private String method;

    private String path;

    private HttpHeaders httpHeaders;

    private String body;
}
