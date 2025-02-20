package com.ecommerce.itemservice.common.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        info = @Info(
                title = "상품 서비스",
                description = "상품 관련 API 제공",
                version = "0.0.1"
        )
)
@Configuration
public class SwaggerConfig {
}
