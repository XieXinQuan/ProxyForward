package shop.huanting.client;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Override
    public String toString() {
        Map<String, String> map = httpHeaders.toSingleValueMap();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", type);
        if (method != null) {
            jsonObject.put("method", method);
        }
        if (path != null) {
            jsonObject.put("path", path);
        }
        if (!map.isEmpty()) {
            jsonObject.put("httpHeaders", map);
        }
        if (StringUtils.isNotEmpty(body)) {
            // 判断是不是json
            List<String> list = httpHeaders.get(HttpHeaders.CONTENT_TYPE);
            if (list != null && list.size() > 0 && list.get(0).contains("json")) {
                jsonObject.put("body", JSON.parseObject(body));
            } else {
                jsonObject.put("body", body);
            }
        }
        return JSON.toJSONString(jsonObject);
    }
}
