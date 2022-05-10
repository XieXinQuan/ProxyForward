package shop.huanting.config;

import entry.ModifyInfo;
import entry.ModifyRequestBody;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import shop.huanting.client.ReceiveServer;
import shop.huanting.client.RemoteServerSend;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author xie.xinquan
 * @create 2022/5/5 10:27
 * @company 深圳亿起融网络科技有限公司
 */
@Component
@Slf4j
public class ModifyRequestBodyFilter {

    @Value("${custom.url}")
    private List<String> url;
    @Resource
    private RemoteServerSend remoteServerSend;
    @Resource
    private ReceiveServer receiveServer;

    public String modifyRequest(ServerWebExchange exchange, String str) {
        String path = exchange.getRequest().getPath().toString();

        // 上报服务器
        remoteServerSend.sendDataToServer(exchange.getRequest().getMethodValue(),
                exchange.getRequest().getPath().value(), exchange.getRequest().getHeaders(), str);

        if (StringUtils.isEmpty(str)) {
            return Strings.EMPTY;
        }
        log.info("Request Body : {}", str);

        ModifyInfo modifyInfo = receiveServer.getModifyInfo(path);
        if (modifyInfo != null && modifyInfo.getModifyRequestBody() != null && StringUtils.isNotEmpty(modifyInfo.getModifyRequestBody().getValue())) {
            ModifyRequestBody modifyRequestBody = modifyInfo.getModifyRequestBody();
            if (StringUtils.isEmpty(modifyRequestBody.getMatchValue()) || modifyRequestBody.getMatchValue().equals(str)) {
                log.info("Modify  Body : {}", modifyRequestBody.getValue());
                return modifyRequestBody.getValue();
            }
        }
        return str;
    }
}
