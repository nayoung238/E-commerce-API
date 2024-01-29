package com.nayoung.orderservice.domain.service;

import com.nayoung.orderservice.domain.Order;
import com.nayoung.orderservice.domain.OrderItemStatus;
import com.nayoung.orderservice.domain.repository.OrderRedisRepository;
import com.nayoung.orderservice.domain.repository.OrderRepository;
import com.nayoung.orderservice.kafka.streams.KStreamKTableJoinConfig;
import com.nayoung.orderservice.kafka.producer.KafkaProducerService;
import com.nayoung.orderservice.kafka.producer.KafkaProducerConfig;
import com.nayoung.orderservice.openfeign.ItemServiceClient;
import com.nayoung.orderservice.web.dto.OrderDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * KStream(주문에 대한 재고 변경 결과) + KTable(waiting 상태의 주문) Join 한 결과(주문 상세)를 DB에 insert 하는 방식 (v2)
 * 주문 생성을 위해 DB 한 번 접근 (insert)
 */
@Service @Slf4j
public class OrderServiceV2 extends OrderService {

    public OrderServiceV2(OrderRepository orderRepository, OrderRedisRepository orderRedisRepository,
                          KafkaProducerService kafkaProducerService,
                          ItemServiceClient itemServiceClient) {
        super(orderRepository, orderRedisRepository, kafkaProducerService, itemServiceClient);
    }

    @Override
    @Transactional
    public OrderDto create(OrderDto orderDto) {
        orderDto.setEventId(setEventId(orderDto.getCustomerAccountId()));
        orderDto.initializeRequestedAt();
        orderDto.setOrderStatus(OrderItemStatus.WAITING);
        kafkaProducerService.send(KafkaProducerConfig.TEMPORARY_ORDER_TOPIC, orderDto.getEventId(), orderDto);
        return orderDto;
    }

    @KafkaListener(topics = KStreamKTableJoinConfig.FINAL_ORDER_CREATION_TOPIC)
    public void create(ConsumerRecord<String, OrderDto> record) {
        log.info("Consuming message success -> Topic: {}, Event Id: {}, Order Status: {}",
                record.topic(),
                record.value().getEventId(),
                record.value().getOrderStatus());

        boolean isExist = orderRepository.existsByEventId(record.key());
        if(!isExist) {
            Order order = Order.fromFinalOrderDto(record.value());
            order.getOrderItems()
                    .forEach(o -> o.setOrder(order));

            orderRepository.save(order);

            // Tombstone record 설정
            kafkaProducerService.send(KafkaProducerConfig.TEMPORARY_ORDER_TOPIC, order.getEventId(), null);
        }
    }

    @Override
    @KafkaListener(topics = {KafkaProducerConfig.TEMPORARY_ORDER_TOPIC,
                            KafkaProducerConfig.RETRY_TEMPORARY_ORDER_TOPIC})
    public void checkFinalStatusOfOrder(ConsumerRecord<String, OrderDto> record) {
        if(record.key() != null && record.value() != null) {
            log.info("Consuming message success -> Topic: {}, Key(event Id): {}",
                    record.topic(),
                    record.key());

            try {
                waitBasedOnTimestamp(record.timestamp());

                boolean isExist = orderRepository.existsByEventId(record.key());
                if (!isExist) {
                    kafkaProducerService.send(KafkaProducerConfig.ORDER_PROCESSING_RESULT_REQUEST_TOPIC, record.key(), record.value());
                }
            } catch (InterruptedException e) {
                log.error(e.getMessage());
            }
        }
    }

    @Override
    @KafkaListener(topics = KafkaProducerConfig.ORDER_PROCESSING_RESULT_REQUEST_TOPIC)
    public void requestOrderItemUpdateResult(ConsumerRecord<String, OrderDto> record) {
        assert record.key() != null & record.value() != null;
        log.info("Consuming message success -> Topic: {}, Key(event Id): {}",
                record.topic(),
                record.key());

        OrderItemStatus orderItemStatus = getOrderStatusByEventId(record.key());
        if(!orderItemStatus.equals(OrderItemStatus.NOT_EXIST)) {
            OrderDto orderDto = OrderDto.fromEventIdAndOrderItemStatus(record.key(), orderItemStatus);
            kafkaProducerService.send(KStreamKTableJoinConfig.ORDER_ITEM_UPDATE_RESULT_TOPIC, record.key(), orderDto);
        }
        else resendKafkaMessage(record.key(), record.value());
    }

    @Override
    public void updateOrderStatusByEventId(String eventId, OrderItemStatus orderItemStatus) {
        OrderDto orderDto = OrderDto.fromEventIdAndOrderItemStatus(eventId, orderItemStatus);
        kafkaProducerService.send(KStreamKTableJoinConfig.ORDER_ITEM_UPDATE_RESULT_TOPIC, eventId, orderDto);
    }
}
