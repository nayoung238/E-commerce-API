package com.ecommerce.itemservice.kafka.config;

import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

public class TopicConfig {

    public static final String ITEM_UPDATE_LOG_TOPIC = "e-commerce.item.item-update-log";
    public static final String ITEM_UPDATE_RESULT_TOPIC = "e-commerce.item.item-update-result";
    public static final String ITEM_UPDATE_RESULT_STREAMS_ONLY_TOPIC = "e-commerce.item.item-update-result-streams-only";

    @Bean
    public KafkaAdmin.NewTopics newTopics() {
        return new KafkaAdmin.NewTopics(
                TopicBuilder.name(ITEM_UPDATE_RESULT_TOPIC)
                        .partitions(1)
                        .replicas(1)
                        .build()
        );
    }
}
