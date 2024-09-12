package com.ecommerce.orderservice.kafka.service.consumer;

import com.ecommerce.orderservice.domain.order.service.OrderCreationByDBServiceImpl;
import com.ecommerce.orderservice.domain.order.service.OrderCreationByKafkaStreamsJoinServiceImpl;
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

    private final OrderCreationByDBServiceImpl orderCreationByDBService;
    private final OrderCreationByKafkaStreamsJoinServiceImpl orderCreationByKafkaStreamsJoinService;

    @KafkaListener(topics = TopicConfig.ORDER_PROCESSING_RESULT_TOPIC)
    private void listenOrderProcessingResultTopic(ConsumerRecord<String, OrderKafkaEvent> record) {
        log.info("Consuming message success -> Topic: {}, OrderEventId: {}, OrderStatus: {}",
                record.topic(),
                record.value().getOrderEventId(),
                record.value().getOrderProcessingStatus());

        orderCreationByDBService.updateOrderStatus(record.value());
    }

    @KafkaListener(topics = TopicConfig.FINAL_ORDER_STREAMS_ONLY_TOPIC)
    public void listenFinalOrderStreamsOnlyTopic(ConsumerRecord<String, OrderKafkaEvent> record) {
        log.info("Consuming message success -> Topic: {}, OrderEventId(Key of KStream-KTable Join): {}, OrderStatus: {}",
                record.topic(),
                record.value().getOrderEventId(),
                record.value().getOrderProcessingStatus());

        orderCreationByKafkaStreamsJoinService.insertFinalOrder(record.value());
    }

    @KafkaListener(topics = {
            TopicConfig.REQUESTED_ORDER_TOPIC,
            TopicConfig.REQUESTED_ORDER_STREAMS_ONLY_TOPIC
    })
    public void listenRequestedOrderTopic(ConsumerRecord<String, OrderKafkaEvent> record) {
        if(record.value() != null) {
            log.info("Consuming message success -> Topic: {}, OrderEventId(Key of KStream-KTable Join): {}",
                    record.topic(),
                    record.key());

            if(record.topic().equals(TopicConfig.REQUESTED_ORDER_TOPIC))
                orderCreationByDBService.checkFinalStatusOfOrder(record.value(), record.timestamp());
            else if(record.key() != null)
                orderCreationByKafkaStreamsJoinService.checkFinalStatusOfOrder(record.value(), record.timestamp());
        }
    }

    @KafkaListener(topics = {
            TopicConfig.ORDER_PROCESSING_RESULT_REQUEST_TOPIC,
            TopicConfig.ORDER_PROCESSING_RESULT_REQUEST_STREAMS_ONLY_TOPIC
    })
    public void listenOrderProcessingResultRequestTopic(ConsumerRecord<String, OrderKafkaEvent> record) {
        log.info("Consuming message success -> Topic: {}, OrderEventId(Key of KStream-KTable Join): {}",
                record.topic(),
                record.value().getOrderEventId());

        if(record.topic().equals(TopicConfig.ORDER_PROCESSING_RESULT_REQUEST_TOPIC))
            orderCreationByDBService.requestOrderProcessingResult(record.value());
        else
            orderCreationByKafkaStreamsJoinService.requestOrderProcessingResult(record.value());
    }
}
