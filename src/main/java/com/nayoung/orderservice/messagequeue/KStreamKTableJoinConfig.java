package com.nayoung.orderservice.messagequeue;

import com.nayoung.orderservice.domain.OrderItemStatus;
import com.nayoung.orderservice.web.dto.OrderDto;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;

import java.util.HashMap;
import java.util.Properties;

@EnableKafka
@Configuration
public class KStreamKTableJoinConfig {

    private final String APPLICATION_ID_CONFIG = "order_application";
    public static final String FINAL_ORDER_CREATION_TOPIC = "e-commerce.order.final-order-details";
    public static final String ITEM_UPDATE_RESULT_TOPIC = "e-commerce.item.item-update-result";

    @Bean
    public Properties kstramProperties() {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, APPLICATION_ID_CONFIG);
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, OrderSerde.class);
        return props;
    }

    @Bean
    public void createOrder() {
        StreamsBuilder builder = new StreamsBuilder();
        KTable<String, OrderDto> orderKTable = builder.table(KafkaProducerConfig.TEMPORARY_ORDER_TOPIC);
        KStream<String, OrderDto> itemUpdateResultStream = builder.stream(ITEM_UPDATE_RESULT_TOPIC);

        itemUpdateResultStream.join(orderKTable, (result, order) -> setOrderStatus(order, result))
                .to(FINAL_ORDER_CREATION_TOPIC);

        KafkaStreams kafkaStreams = new KafkaStreams(builder.build(), kstramProperties());
        kafkaStreams.start();
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
