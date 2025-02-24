package com.ecommerce.apigatewayservice.common.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RouterConfig {

    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route(r -> r.path("/item-api/**")
                        .filters(f -> f.rewritePath("/item-api/(?<segment>.*)", "/${segment}"))
                        .uri("lb://ITEM-SERVICE"))
                .route(r -> r.path("/order-api/**")
                        .filters(f -> f.rewritePath("/order-api/(?<segment>.*)", "/${segment}"))
                        .uri("lb://ORDER-SERVICE"))
                .route(r -> r.path("/account-api/**")
                        .filters(f -> f.rewritePath("/account-api/(?<segment>.*)", "/${segment}"))
                        .uri("lb://ACCOUNT-SERVICE"))
                .route(r -> r.path("/coupon-api/**")
                        .filters(f -> f.rewritePath("/coupon-api/(?<segment>.*)", "/${segment}"))
                        .uri("lb://COUPON-SERVICE"))
                .route(r -> r.path("/my-page/**")
                        .uri("lb://API-COMPOSER"))
                .build();
    }
}
