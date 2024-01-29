package com.nayoung.orderservice.kafka.producer;

import com.nayoung.orderservice.kafka.dto.OrderDtoSerializer;
import com.nayoung.orderservice.web.dto.OrderDto;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
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

    public static final String TEMPORARY_ORDER_TOPIC = "e-commerce.order.temporary-order-details";
    public static final String  RETRY_TEMPORARY_ORDER_TOPIC = "e-commerce.order.retry-temporary-order-details";
    public static final String ORDER_PROCESSING_RESULT_REQUEST_TOPIC = "e-commerce.order.order-processing-result-request";

    @Bean
    public ProducerFactory<String, OrderDto> orderDtoProducerFactory() {
        Map<String, Object> properties = new HashMap<>();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, OrderDtoSerializer.class);
        properties.put(ProducerConfig.ACKS_CONFIG, "1");
        return new DefaultKafkaProducerFactory<>(properties);
    }

    @Bean
    public KafkaTemplate<String, OrderDto> orderDtoTemplate() {
        return new KafkaTemplate<>(orderDtoProducerFactory());
    }
}
