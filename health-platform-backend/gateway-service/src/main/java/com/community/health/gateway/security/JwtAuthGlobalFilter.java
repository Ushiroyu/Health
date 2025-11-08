package com.community.health.gateway.security;

import com.community.health.common.security.JwtUser;
import com.community.health.common.security.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * Gateway 全局 JWT 认证过滤器
 * - 读取配置中的 secret/ttl/whitelist
 * - 对非白名单请求强制校验 JWT
 * - 将用户信息透传为统一请求头：X-User-Id / X-Username / X-User-Role
 */
@Component
public class JwtAuthGlobalFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;
    private final List<String> whitelist;
    private final AntPathMatcher matcher = new AntPathMatcher();

    public JwtAuthGlobalFilter(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.ttl:86400000}") long ttl,
            @Value("${security.whitelist:/auth,/actuator}") String whiteCsv
    ) {
        this.jwtUtil = new JwtUtil(secret, ttl);
        String normalized = whiteCsv == null ? "" : whiteCsv.replace("[", "").replace("]", "");
        this.whitelist = Arrays.stream(normalized.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .toList();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        if (isWhitelisted(path)) {
            return chain.filter(exchange);
        }

        String auth = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (auth == null || !auth.startsWith("Bearer ")) {
            return unauthorized(exchange, "Missing Authorization token");
        }

        String token = auth.substring(7);
        if (!jwtUtil.validateToken(token)) {
            return unauthorized(exchange, "Invalid token");
        }

        JwtUser user = jwtUtil.parseUser(token);
        if (user == null || user.getUserId() == null) {
            return unauthorized(exchange, "Invalid token payload");
        }

        ServerWebExchange mutated = exchange.mutate()
                .request(r -> r
                        .headers(h -> {
                            h.set("X-User-Id", String.valueOf(user.getUserId()));
                            h.set("X-Username", user.getUsername() == null ? "" : user.getUsername());
                            h.set("X-User-Role", user.getRole() == null ? "" : user.getRole());
                        })
                )
                .build();
        return chain.filter(mutated);
    }

    private boolean isWhitelisted(String path) {
        for (String p : whitelist) {
            if (!StringUtils.hasText(p)) continue;
            // 支持前缀与通配符
            if (path.startsWith(p)) return true;
            if (matcher.match(p, path)) return true;
        }
        return false;
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = "{\"code\":401,\"message\":\"" + message + "\",\"data\":null}";
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(bytes)));
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
