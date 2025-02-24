package com.ecommerce.apicomposer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;

@SpringBootApplication
@EnableFeignClients("com.ecommerce.apicomposer")
@ImportAutoConfiguration({FeignAutoConfiguration.class})
public class ApiComposerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiComposerApplication.class, args);
	}

}
