package com.nayoung.orderservice.kafka.streams;

import com.nayoung.orderservice.domain.OrderItemStatus;
import com.nayoung.orderservice.kafka.dto.OrderSerde;
import com.nayoung.orderservice.kafka.producer.KafkaProducerConfig;
import com.nayoung.orderservice.kafka.producer.KafkaProducerService;
import com.nayoung.orderservice.web.dto.OrderDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration;
import org.springframework.kafka.config.KafkaStreamsConfiguration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EnableKafkaStreams
@Configuration
@RequiredArgsConstructor
@Slf4j
public class KStreamKTableJoinConfig {

    public static final String FINAL_ORDER_CREATION_TOPIC = "e-commerce.order.final-order-details";
    public static final String ORDER_PROCESSING_RESULT_TOPIC = "e-commerce.item.item-update-result";
    private final String STATE_DIR = "/tmp/kafka-streams/";
    private final KafkaProducerService kafkaProducerService;

    @Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
    public KafkaStreamsConfiguration kafkaStreamsProperties() {
        Map<String, Object> props = new HashMap<>();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "order_application");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, OrderSerde.class);

        String[] split = UUID.randomUUID().toString().split("-");
        props.put(StreamsConfig.STATE_DIR_CONFIG, STATE_DIR + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm")) + "-" + split[0]);
        return new KafkaStreamsConfiguration(props);
    }

    @Bean
    public KTable<String, OrderDto> requestedOrder(StreamsBuilder streamsBuilder) {
        return streamsBuilder.table(KafkaProducerConfig.TEMPORARY_ORDER_TOPIC);
    }

    @Bean
    public KStream<String, OrderDto> orderProcessingResult(StreamsBuilder streamsBuilder) {
        return streamsBuilder.stream(ORDER_PROCESSING_RESULT_TOPIC);
    }

    @Bean
    public KStream<String, OrderDto> createOrder(KTable<String, OrderDto> requestedOrder,
                                                 KStream<String, OrderDto> orderProcessingResult) {
        return orderProcessingResult
                .filter((key, value) -> {
                    if(!value.getOrderStatus().equals(OrderItemStatus.SUCCEEDED)
                            && !value.getOrderStatus().equals(OrderItemStatus.FAILED)) {
                        log.error("Order status of {} -> {}", key, value.getOrderStatus());
                        kafkaProducerService.setTombstoneRecord(KafkaProducerConfig.TEMPORARY_ORDER_TOPIC, key);
                    }
                    return value.getOrderStatus().equals(OrderItemStatus.SUCCEEDED)
                            || value.getOrderStatus().equals(OrderItemStatus.FAILED);
                })
                .join(requestedOrder, (result, order) -> setOrderStatus(order, result));
    }

    private OrderDto setOrderStatus(OrderDto orderDto, OrderDto result) {
        orderDto.updateOrderStatus(result);
        return orderDto;
    }
}
