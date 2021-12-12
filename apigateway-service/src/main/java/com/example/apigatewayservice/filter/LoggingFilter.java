package com.example.apigatewayservice.filter;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.OrderedGatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class LoggingFilter extends AbstractGatewayFilterFactory<LoggingFilter.Config> {

    public LoggingFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        /*return ((exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();  //exchane로  request, response를 받을 수 있다.
            ServerHttpResponse response = exchange.getResponse();

            log.info("Global Filter Base Message : {}", config.getBaseMessage());

            if(config.isPreLogger()) {
                log.info("Global Filter Start : Request ID -> {}", request.getId());
            }

            // Custom Post Filter
            return chain.filter(exchange).then(Mono.fromRunnable(() -> { // 비동기방식에서 단일 값 추출할떄 Mono
                if(config.isPostLogger()) {
                    log.info("Global Filter End : response code -> {}", response.getStatusCode());
                }

            }));
        });*/
        GatewayFilter filter = new OrderedGatewayFilter((exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();  //exchane로  request, response를 받을 수 있다.
            ServerHttpResponse response = exchange.getResponse();

            log.info("Logging Filter Base Message : {}", config.getBaseMessage());

            if (config.isPreLogger()) {
                log.info("Logging Filter Start : Request ID -> {}", request.getId());
            }

            // Custom Post Filter
            return chain.filter(exchange).then(Mono.fromRunnable(() -> { // 비동기방식에서 단일 값 추출할떄 Mono
                if (config.isPostLogger()) {
                    log.info("Logging Filter End : response code -> {}", response.getStatusCode());
                }

            }));
        }, Ordered.LOWEST_PRECEDENCE); // GateWayFilter

        return filter;
    }

    @Data
    public static class Config {
        // Put the configuration properties
        private String baseMessage;
        private boolean preLogger;
        private boolean postLogger;
    }
}
