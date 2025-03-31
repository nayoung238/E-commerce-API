package com.ecommerce.orderservice.kafka.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

@Configuration
public class TopicConfig {

    public static final String REQUESTED_ORDER_TOPIC = "e-commerce.order.requested-order-details";
    public static final String REQUESTED_ORDER_STREAMS_ONLY_TOPIC = "e-commerce.order.requested-order-details-streams-only";
    public static final String ORDER_PROCESSED_RESULT_REQUEST_TOPIC = "e-commerce.order.order-processed-result-request";
    public static final String ORDER_PROCESSED_RESULT_REQUEST_STREAMS_ONLY_TOPIC = "e-commerce.order.order-processed-result-request-streams-only";
    public static final String ORDER_PROCESSED_RESULT_TOPIC = "e-commerce.item.item-update-result";
    public static final String ORDER_PROCESSED_RESULT_STREAMS_ONLY_TOPIC = "e-commerce.item.item-update-result-streams-only";
    public static final String FINAL_ORDER_STREAMS_ONLY_TOPIC = "e-commerce.order.final-order-details-streams-only";
    public static final String ORDER_UPDATED_TOPIC = "e-commerce.order.updated-order";

    @Bean
    public KafkaAdmin.NewTopics newTopics() {
        return new KafkaAdmin.NewTopics(
//                TopicBuilder.name(REQUESTED_ORDER_TOPIC)
//                        .partitions(1)
//                        .replicas(1)
//                        .build(),
//
//                TopicBuilder.name(ORDER_PROCESSED_RESULT_REQUEST_TOPIC)
//                        .partitions(1)
//                        .replicas(1)
//                        .build(),

                TopicBuilder.name(ORDER_PROCESSED_RESULT_REQUEST_STREAMS_ONLY_TOPIC)
                        .partitions(1)
                        .replicas(1)
                        .build(),

                TopicBuilder.name(FINAL_ORDER_STREAMS_ONLY_TOPIC)
                        .partitions(1)
                        .replicas(1)
                        .build()
        );
    }
}
