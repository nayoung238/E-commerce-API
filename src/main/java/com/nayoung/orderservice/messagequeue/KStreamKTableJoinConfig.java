package com.nayoung.orderservice.messagequeue;

import com.nayoung.orderservice.domain.OrderItemStatus;
import com.nayoung.orderservice.web.dto.OrderDto;
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

import java.util.HashMap;
import java.util.Map;

@EnableKafkaStreams
@Configuration
public class KStreamKTableJoinConfig {

    public static final String FINAL_ORDER_CREATION_TOPIC = "e-commerce.order.final-order-details";
    public static final String ORDER_ITEM_UPDATE_RESULT_TOPIC = "e-commerce.item.item-update-result";

    @Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
    public KafkaStreamsConfiguration kafkaStreamsProperties() {
        Map<String, Object> props = new HashMap<>();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "order_application");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, OrderSerde.class);
        return new KafkaStreamsConfiguration(props);
    }

    @Bean
    public KStream<String, OrderDto> createOrder(StreamsBuilder streamsBuilder) {
        KTable<String, OrderDto> requestedOrder = streamsBuilder.table(KafkaProducerConfig.TEMPORARY_ORDER_TOPIC);
        KStream<String, OrderDto> itemUpdateResult = streamsBuilder.stream(ORDER_ITEM_UPDATE_RESULT_TOPIC);

        return itemUpdateResult.join(requestedOrder, (result, order) -> setOrderStatus(order, result));
    }

    private OrderDto setOrderStatus(OrderDto orderDto, OrderDto result) {
        orderDto.setOrderStatus(result.getOrderStatus());

        // Key: item ID, value: order item status
        HashMap<Long, OrderItemStatus> orderItemStatusHashMap = new HashMap<>();
        result.getOrderItemDtos()
                .forEach(o -> orderItemStatusHashMap.put(o.getItemId(), o.getOrderItemStatus()));

        orderDto.getOrderItemDtos()
                .forEach(o -> o.setOrderStatus(orderItemStatusHashMap.get(o.getItemId())));

        return orderDto;
    }
}
