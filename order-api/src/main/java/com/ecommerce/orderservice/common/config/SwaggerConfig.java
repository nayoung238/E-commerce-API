package com.ecommerce.orderservice.common.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        info = @Info(
                title = "주문 서비스",
                description = "주문 관련 API 제공",
                version = "0.0.1"
        )
)
@Configuration
public class SwaggerConfig {
}
