package com.ecommerce.orderservice.kafka.config.streams;

import com.ecommerce.orderservice.domain.order.OrderStatus;
import com.ecommerce.orderservice.kafka.config.TopicConfig;
import com.ecommerce.orderservice.kafka.dto.OrderKafkaEvent;
import com.ecommerce.orderservice.kafka.dto.OrderEventSerde;
import com.ecommerce.orderservice.kafka.service.producer.KafkaProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration;
import org.springframework.kafka.config.KafkaStreamsConfiguration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EnableKafkaStreams
@Configuration
@RequiredArgsConstructor
@Slf4j
public class KStreamKTableJoinConfig {

    private final String STATE_DIR = "/tmp/kafka-streams/";
    private final KafkaProducerService kafkaProducerService;

    @Value("${spring.kafka.streams.application-id}")
    private String STREAM_APPLICATION_ID;

    @Value("${spring.kafka.bootstrap-servers}")
    private String BOOTSTRAP_SERVER;

    @Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
    public KafkaStreamsConfiguration kafkaStreamsProperties() {
        Map<String, Object> props = new HashMap<>();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, STREAM_APPLICATION_ID);
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVER);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, OrderEventSerde.class);

        String[] split = UUID.randomUUID().toString().split("-");
        props.put(StreamsConfig.STATE_DIR_CONFIG, STATE_DIR + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm")) + "-" + split[0]);
        return new KafkaStreamsConfiguration(props);
    }

    @Bean
    public KTable<String, OrderKafkaEvent> requestedOrder(KafkaStreamsConfiguration kafkaStreamsConfiguration,
                                                          StreamsBuilder streamsBuilder) {
        AdminClient adminClient = AdminClient.create(kafkaStreamsConfiguration.asProperties());
        adminClient.createTopics(Collections.singleton(
                new NewTopic(TopicConfig.REQUESTED_ORDER_STREAMS_ONLY_TOPIC, 1, (short) 1)));

        return streamsBuilder.table(TopicConfig.REQUESTED_ORDER_STREAMS_ONLY_TOPIC);
    }

    @Bean
    public KStream<String, OrderKafkaEvent> orderProcessingResult(KafkaStreamsConfiguration kafkaStreamsConfiguration,
                                                                  StreamsBuilder streamsBuilder) {
        AdminClient adminClient = AdminClient.create(kafkaStreamsConfiguration.asProperties());
        adminClient.createTopics(Collections.singleton(
                new NewTopic(TopicConfig.ORDER_PROCESSING_RESULT_STREAMS_ONLY_TOPIC, 1, (short) 1)));

        return streamsBuilder.stream(TopicConfig.ORDER_PROCESSING_RESULT_STREAMS_ONLY_TOPIC);
    }

    @Bean
    public KStream<String, OrderKafkaEvent> createOrder(KTable<String, OrderKafkaEvent> requestedOrder,
                                                        KStream<String, OrderKafkaEvent> orderProcessingResult) {
        return orderProcessingResult
                .filter((key, value) -> {
                    if(!value.getOrderStatus().equals(OrderStatus.SUCCEEDED)
                            && !value.getOrderStatus().equals(OrderStatus.FAILED)) {
                        log.warn("Order status of {} -> {}", key, value.getOrderStatus());
                        kafkaProducerService.setTombstoneRecord(TopicConfig.REQUESTED_ORDER_STREAMS_ONLY_TOPIC, key);
                    }
                    return value.getOrderStatus().equals(OrderStatus.SUCCEEDED)
                            || value.getOrderStatus().equals(OrderStatus.FAILED);
                })
                .join(requestedOrder, (result, order) -> setOrderStatus(order, result));
    }

    private OrderKafkaEvent setOrderStatus(OrderKafkaEvent orderEvent, OrderKafkaEvent result) {
        orderEvent.updateOrderStatus(result);
        return orderEvent;
    }
}
