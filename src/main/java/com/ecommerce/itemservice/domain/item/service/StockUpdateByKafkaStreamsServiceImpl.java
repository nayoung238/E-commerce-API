package com.ecommerce.itemservice.domain.item.service;

import com.ecommerce.itemservice.domain.item.Item;
import com.ecommerce.itemservice.domain.item.repository.ItemRedisRepository;
import com.ecommerce.itemservice.domain.item.repository.ItemRepository;
import com.ecommerce.itemservice.exception.ExceptionCode;
import com.ecommerce.itemservice.kafka.config.TopicConfig;
import com.ecommerce.itemservice.kafka.dto.OrderItemKafkaEvent;
import com.ecommerce.itemservice.kafka.dto.OrderStatus;
import com.ecommerce.itemservice.kafka.service.producer.KafkaProducerService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaProducerException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Kafka Streams 이용해 이벤트 집계 후 DB에 반영하는 방식
 * -> 재고 변경 데이터를 event로 생성 및 Kafka Streams로 이벤트 집계 후 DB 반영
 *
 * Late event까지 고려하는 적절한 Grace Period를 설정할 수 없음
 * -> 네트워크 지연 예측 불가
 * -> 정확한 집계 불가능 (더 이상 해당 방식을 사용하지 않음)
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class StockUpdateByKafkaStreamsServiceImpl implements StockUpdateService {

    private final ItemRepository itemRepository;
    private final ItemRedisRepository itemRedisRepository;
    private final KafkaProducerService kafkaProducerService;

    @Override
    @Transactional
    public OrderItemKafkaEvent updateStock(OrderItemKafkaEvent orderItemKafkaEvent) {
        Item item = itemRepository.findById(orderItemKafkaEvent.getItemId())
                .orElseThrow(() -> new EntityNotFoundException(ExceptionCode.NOT_FOUND_ITEM.getMessage()));

        // Redis에서 재고 차감 시도
        if(isUpdatableStockByRedis(item.getId(), orderItemKafkaEvent.getQuantity())) {
            orderItemKafkaEvent.updateOrderStatus((orderItemKafkaEvent.getQuantity() < 0) ?
                    OrderStatus.SUCCEEDED  // consumption
                    : OrderStatus.CANCELED);  // production (undo)

            sendMessageToKafka(orderItemKafkaEvent.getItemId(), orderItemKafkaEvent.getQuantity());
        } else {
            orderItemKafkaEvent.updateOrderStatus(OrderStatus.OUT_OF_STOCK);
        }
        return orderItemKafkaEvent;
    }

    private boolean isUpdatableStockByRedis(Long itemId, Long quantity) {
        Long stock = itemRedisRepository.incrementItemStock(itemId, quantity);
        if(stock >= 0) return true;

        itemRedisRepository.decrementItemStock(itemId, quantity);   // undo
        return false;
    }

    private void sendMessageToKafka(Long itemId, Long quantity) {
        try {
            kafkaProducerService.sendMessage(TopicConfig.ITEM_UPDATE_LOG_TOPIC, String.valueOf(itemId), quantity);
        } catch(KafkaProducerException e) {
            log.error("Kafka Exception " + e.getMessage());
            // TODO: broker에 적재되지 못한 이벤트 처리
        }
    }
}
