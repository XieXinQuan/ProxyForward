package shop.huanting.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.RouteToRequestUrlFilter;
import org.springframework.cloud.gateway.filter.factory.rewrite.CachedBodyOutputMessage;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyRequestBodyGatewayFilterFactory;
import org.springframework.cloud.gateway.support.BodyInserterContext;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.codec.HttpMessageReader;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import shop.huanting.client.RemoteServerSend;

import javax.annotation.Resource;
import java.util.List;
import java.util.function.Function;

/**
 * @author xie.xinquan
 * @create 2022/5/5 10:27
 * @company 深圳亿起融网络科技有限公司
 */
@Component
@Slf4j
public class ModifyRequestBody {

    @Value("${custom.url}")
    private List<String> url;
    @Resource
    private RemoteServerSend remoteServerSend;

    public String modifyRequest(ServerWebExchange exchange, String str) {
        String path = exchange.getRequest().getPath().toString();

        // 上报服务器
        remoteServerSend.sendDataToServer(exchange.getRequest().getMethodValue(),
                exchange.getRequest().getPath().value(), exchange.getRequest().getHeaders(), str);


        if (!StringUtils.hasLength(str)) {
            return Strings.EMPTY;
        }
        log.info("Request Body : {}", str);
        if ("/bigDataVisual/queryInvoiceVisual".equals(path)) {
            str = "{\"projectId\":499,\"templateId\":1920}";
        }else if ("/bigDataVisual/queryCallVisual".equals(path)) {
            str = "{\"projectId\":499,\"templateId\":1922}";
        }

        log.info("Modify  Body : {}", str);
        return str;
    }
}
