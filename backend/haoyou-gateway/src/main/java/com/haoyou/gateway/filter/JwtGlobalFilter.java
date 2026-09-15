package com.haoyou.gateway.filter;

import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * 网关全局过滤器骨架：校验 Authorization 头中的 JWT。
 * 当前放行，待接入登录平台后补全鉴权逻辑（无 Token 或校验失败返回 401）。
 */
@Component
public class JwtGlobalFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(org.springframework.web.server.ServerWebExchange exchange,
                             org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {
        // TODO: 校验 JWT，失败时直接返回 401
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
