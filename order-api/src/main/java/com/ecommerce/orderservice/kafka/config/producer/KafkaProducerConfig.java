package com.ecommerce.orderservice.kafka.config.producer;

import com.ecommerce.orderservice.kafka.dto.OrderKafkaEvent;
import com.ecommerce.orderservice.kafka.dto.OrderEventSerializer;
import com.ecommerce.orderservice.kafka.dto.updatedEvent.OrderUpdatedEvent;
import com.ecommerce.orderservice.kafka.dto.updatedEvent.OrderUpdatedEventSerializer;
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

    @Value("${spring.kafka.bootstrap-servers}")
    private String BOOTSTRAP_SERVER;

    @Bean
    public ProducerFactory<String, OrderKafkaEvent> orderEventProducerFactory() {
        Map<String, Object> properties = new HashMap<>();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVER);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, OrderEventSerializer.class);
        properties.put(ProducerConfig.ACKS_CONFIG, "1");
        return new DefaultKafkaProducerFactory<>(properties);
    }

    @Bean
    public KafkaTemplate<String, OrderKafkaEvent> orderKafkaEventKafkaTemplate() {
        return new KafkaTemplate<>(orderEventProducerFactory());
    }

    @Bean
    public ProducerFactory<String, OrderUpdatedEvent> orderUpdatedEventProducerFactory() {
        Map<String, Object> properties = new HashMap<>();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVER);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, OrderUpdatedEventSerializer.class);
        properties.put(ProducerConfig.ACKS_CONFIG, "1");
        return new DefaultKafkaProducerFactory<>(properties);
    }

    @Bean
    public KafkaTemplate<String, OrderUpdatedEvent> orderUpdatedEventKafkaTemplate() {
        return new KafkaTemplate<>(orderUpdatedEventProducerFactory());
    }
}
