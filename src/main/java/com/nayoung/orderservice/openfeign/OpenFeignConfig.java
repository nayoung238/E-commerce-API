package com.nayoung.orderservice.openfeign;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = "com.nayoung.orderservice.openfeign")
public class OpenFeignConfig {
}
