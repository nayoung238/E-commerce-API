package com.ecommerce.orderservice.kafka.service.consumer;

import com.ecommerce.orderservice.order.service.OrderCreationDbServiceImpl;
import com.ecommerce.orderservice.order.service.OrderCreationStreamsServiceImpl;
import com.ecommerce.orderservice.kafka.config.TopicConfig;
import com.ecommerce.orderservice.kafka.dto.OrderKafkaEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaConsumerService {

    private final OrderCreationDbServiceImpl orderCreationByDBService;
    private final OrderCreationStreamsServiceImpl orderCreationByKafkaStreamsJoinService;

    @KafkaListener(topics = TopicConfig.ORDER_PROCESSED_RESULT_TOPIC)
    private void listenOrderProcessedResultTopic(ConsumerRecord<String, OrderKafkaEvent> record) {
        log.info("Event consumed successfully -> Topic: {}, OrderEventId: {}, OrderStatus: {}",
                record.topic(),
                record.value().getOrderEventId(),
                record.value().getOrderStatus());

        orderCreationByDBService.updateOrderStatus(record.value());
    }

    @KafkaListener(topics = TopicConfig.FINAL_ORDER_STREAMS_ONLY_TOPIC)
    public void listenFinalOrderStreamsOnlyTopic(ConsumerRecord<String, OrderKafkaEvent> record) {
        log.info("Event consumed successfully -> Topic: {}, OrderEventId(Key of KStream-KTable Join): {}, OrderStatus: {}",
                record.topic(),
                record.value().getOrderEventId(),
                record.value().getOrderStatus());

        orderCreationByKafkaStreamsJoinService.insertFinalOrder(record.value());
    }

    @KafkaListener(topics = {
            TopicConfig.REQUESTED_ORDER_TOPIC,
            TopicConfig.REQUESTED_ORDER_STREAMS_ONLY_TOPIC
    })
    public void listenRequestedOrderTopic(ConsumerRecord<String, OrderKafkaEvent> record) {
        if(record.value() != null) {
            log.info("Event consumed successfully -> Topic: {}, OrderEventId(Key of KStream-KTable Join): {}",
                    record.topic(),
                    record.key());

            if (record.topic().equals(TopicConfig.REQUESTED_ORDER_TOPIC)) {
                orderCreationByDBService.checkFinalStatusOfOrder(record.value(), record.timestamp());
            }
            else if (record.key() != null) {
                orderCreationByKafkaStreamsJoinService.checkFinalStatusOfOrder(record.value(), record.timestamp());
            }
        }
    }

    @KafkaListener(topics = {
            TopicConfig.ORDER_PROCESSED_RESULT_REQUEST_TOPIC,
            TopicConfig.ORDER_PROCESSED_RESULT_REQUEST_STREAMS_ONLY_TOPIC
    })
    public void listenOrderProcessedResultRequestTopic(ConsumerRecord<String, OrderKafkaEvent> record) {
        log.info("Event consumed successfully -> Topic: {}, OrderEventId(Key of KStream-KTable Join): {}",
                record.topic(),
                record.value().getOrderEventId());

        if (record.topic().equals(TopicConfig.ORDER_PROCESSED_RESULT_REQUEST_TOPIC)) {
            orderCreationByDBService.requestOrderProcessedResult(record.value());
        }
        else {
            orderCreationByKafkaStreamsJoinService.requestOrderProcessedResult(record.value());
        }
    }
}
