package com.ecommerce.accountservice.kafka.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

@Configuration
public class TopicConfig {

    public static final String COUPON_ISSUANCE_RESULT_TOPIC = "e-commerce.coupon.coupon-issuance-result";

    @Bean
    public KafkaAdmin.NewTopics newTopics() {
        return new KafkaAdmin.NewTopics(
                TopicBuilder.name(COUPON_ISSUANCE_RESULT_TOPIC)
                        .partitions(1)
                        .replicas(1)
                        .build()
        );
    }
}
