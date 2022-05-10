package shop.huanting.config;

import entry.AddHeader;
import entry.DeleteHeader;
import entry.ModifyHeader;
import entry.ModifyInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import shop.huanting.client.ReceiveServer;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

/**
 * @author xie.xinquan
 * @create 2022/5/9 16:18
 * @company 深圳亿起融网络科技有限公司
 */
@Slf4j
@Component
public class ModifyRequestHeaderFilter implements GlobalFilter, Ordered {

    @Resource
    private ReceiveServer receiveServer;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String path = exchange.getRequest().getPath().value();
        ModifyInfo modifyInfo = receiveServer.getModifyInfo(path);
        if (modifyInfo == null) {
            chain.filter(exchange);
        }



        ServerHttpRequest request = exchange.getRequest().mutate()
                .headers(httpHeaders -> {

                    if (modifyInfo == null) {
                        return;
                    }

                    // del > modify > add

                    List<DeleteHeader> deleteHeaders = modifyInfo.getDeleteHeaders();
                    if (CollectionUtils.isNotEmpty(deleteHeaders)) {
                        for (DeleteHeader deleteHeader : deleteHeaders) {
                            if (StringUtils.isEmpty(deleteHeader.getHeader()) || !httpHeaders.containsKey(deleteHeader.getHeader())) {
                                continue;
                            }
                            if (StringUtils.isEmpty(deleteHeader.getMatchValue()) || deleteHeader.getMatchValue().equals(httpHeaders.get(deleteHeader.getHeader()).get(0))) {
                                httpHeaders.remove(deleteHeader.getHeader());
                            }
                        }
                    }

                    List<ModifyHeader> modifyHeaders = modifyInfo.getModifyHeaders();
                    if (CollectionUtils.isNotEmpty(modifyHeaders)) {
                        for (ModifyHeader modifyHeader : modifyHeaders) {
                            if (StringUtils.isEmpty(modifyHeader.getHeader()) || StringUtils.isEmpty(modifyHeader.getValue()) || !httpHeaders.containsKey(modifyHeader.getHeader())) {
                                continue;
                            }
                            if (StringUtils.isEmpty(modifyHeader.getMatchValue()) || httpHeaders.get(modifyHeader.getHeader()).get(0).equals(modifyHeader.getMatchValue())) {
                                httpHeaders.put(modifyHeader.getHeader(), Arrays.asList(modifyHeader.getValue()));
                            }
                        }
                    }

                    List<AddHeader> addHeaders = modifyInfo.getAddHeaders();
                    if (CollectionUtils.isNotEmpty(addHeaders)) {
                        for (AddHeader addHeader : addHeaders) {
                            if (StringUtils.isNotEmpty(addHeader.getHeader()) && StringUtils.isNotEmpty(addHeader.getValue())
                                    && !httpHeaders.containsKey(addHeader.getHeader())) {
                                httpHeaders.add(addHeader.getHeader(), addHeader.getValue());
                            }
                        }
                    }


                }).build();

        return chain.filter(exchange.mutate().request(request).build());
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
