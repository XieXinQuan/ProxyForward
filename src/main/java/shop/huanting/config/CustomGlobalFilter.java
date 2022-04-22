package shop.huanting.config;

import io.netty.buffer.Unpooled;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.RouteToRequestUrlFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Pattern;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.*;


/**
 * @author xie.xinquan
 * @create 2022/4/22 9:50
 * @company 深圳亿起融网络科技有限公司
 */
@Slf4j
@Component
public class CustomGlobalFilter implements GlobalFilter, Ordered {
    private static final String SCHEME_REGEX = "[a-zA-Z]([a-zA-Z]|\\d|\\+|\\.|-)*:.*";
    static final Pattern schemePattern = Pattern.compile(SCHEME_REGEX);
    static boolean hasAnotherScheme(URI uri) {
        return schemePattern.matcher(uri.getSchemeSpecificPart()).matches()
                && uri.getHost() == null && uri.getRawPath() == null;
    }




    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

//        if (true) {
//            return chain.filter(exchange);
//        }

        ServerHttpRequest request = exchange.getRequest();
        log.info(Strings.EMPTY);
        log.info("................................");
        log.info("Method: {}", request.getMethod());
        log.info("Path: {}", request.getPath());
        log.info("URL: {}", request.getURI());
        HttpHeaders headers = request.getHeaders();
        for (String key : headers.keySet()) {
            log.info("{}: {}", key, headers.get(key).size() > 1 ? headers.get(key) : headers.get(key).get(0));
        }
        Flux<DataBuffer> body = request.getBody();
        StringBuilder builder = new StringBuilder();
//        body.
        log.info("Request Body: " + builder.toString());

        URI uri = exchange.getRequest().getURI();
        Route route = exchange.getAttribute(GATEWAY_ROUTE_ATTR);
        URI routeUri = route.getUri();
        boolean encoded = containsEncodedParts(uri);
        if (hasAnotherScheme(routeUri)) {
            // this is a special url, save scheme to special attribute
            // replace routeUri with schemeSpecificPart
            // exchange.getAttributes().put(GATEWAY_SCHEME_PREFIX_ATTR, routeUri.getScheme());
            routeUri = URI.create(routeUri.getSchemeSpecificPart());
        }

        List<String> hostList = request.getHeaders().get("Host");
        if (hostList != null && hostList.size() > 0 && hostList.get(0) != null && hostList.get(0).length() > 0) {
            String host = hostList.get(0);
            if (!host.startsWith("http://")) {
                host = "http://" + host;
            }
            try {
                routeUri = new URI(host);
            }catch (URISyntaxException e) {
                log.error("无法识别的URL： " + host);
            }
        }

        // 替换掉 RouteToRequestUrlFilter的设置
        URI mergedUrl = UriComponentsBuilder.fromUri(uri)
                // .uri(routeUri)
                .scheme(routeUri.getScheme()).host(routeUri.getHost())
                .port(routeUri.getPort()).build(encoded).toUri();
        exchange.getAttributes().put(GATEWAY_REQUEST_URL_ATTR, mergedUrl);

        return chain.filter(exchange);
    }


    @Override
    public int getOrder() {
        return RouteToRequestUrlFilter.ROUTE_TO_URL_FILTER_ORDER + 1;
    }
}