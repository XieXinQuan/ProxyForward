package shop.huanting.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;

/**
 * @author xie.xinquan
 * @create 2022/4/22 11:04
 * @company 深圳亿起融网络科技有限公司
 */
@Configuration
public class GatewayConfig  {

    @Resource
    private ModifyRequestBodyFilter modifyRequestBodyFilter;

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {

        return builder.routes()
                .route("h", r -> r.host("*")
                        .filters(gatewayFilterSpec -> gatewayFilterSpec.modifyRequestBody(byte[].class, byte[].class, (serverWebExchange, arr) -> Mono.just(modifyRequestBodyFilter.modifyRequest(serverWebExchange, arr))))
                .uri("http://192.168.10.228:8092"))
                // 自定义处理类
                .build();
    }

}
