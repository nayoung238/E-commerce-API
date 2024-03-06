package com.ecommerce.orderservice.kafka.producer;

import com.ecommerce.orderservice.kafka.dto.OrderEvent;
import com.ecommerce.orderservice.kafka.dto.OrderEventSerializer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
public class KafkaProducerConfig {

    public static final String REQUESTED_ORDER_TOPIC = "e-commerce.order.requested-order-details";
    public static final String REQUESTED_ORDER_STREAMS_ONLY_TOPIC = "e-commerce.order.requested-order-details-streams-only";
    public static final String ORDER_PROCESSING_RESULT_REQUEST_TOPIC = "e-commerce.order.order-processing-result-request";
    public static final String ORDER_PROCESSING_RESULT_REQUEST_STREAMS_ONLY_TOPIC = "e-commerce.order.order-processing-result-request-streams-only";

    @Value("${spring.kafka.bootstrap-servers}")
    private String BOOTSTRAP_SERVER;

    @Bean
    public ProducerFactory<String, OrderEvent> orderEventProducerFactory() {
        Map<String, Object> properties = new HashMap<>();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVER);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, OrderEventSerializer.class);
        properties.put(ProducerConfig.ACKS_CONFIG, "1");
        return new DefaultKafkaProducerFactory<>(properties);
    }

    @Bean
    public KafkaTemplate<String, OrderEvent> orderDtoTemplate() {
        return new KafkaTemplate<>(orderEventProducerFactory());
    }
}
