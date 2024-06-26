
package com.ecommerce.apigatewayservice;

import com.ecommerce.apigatewayservice.service.mypage.MyPageHandler;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

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
                .build();
    }

//    @Bean
//    public RouterFunction<ServerResponse> myPageRoutes(MyPageHandler myPageHandler) {
//        return RouterFunctions.route()
//                .GET("/my-page-details/{userId}/{cursorOrderId}",
//                        RequestPredicates.accept(MediaType.APPLICATION_JSON),
//                        myPageHandler::getMyPageDetails)
//                .build();
//    }
}
