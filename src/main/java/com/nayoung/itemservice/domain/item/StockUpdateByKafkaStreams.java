package com.nayoung.itemservice.domain.item;

import com.nayoung.itemservice.exception.ExceptionCode;
import com.nayoung.itemservice.exception.ItemException;
import com.nayoung.itemservice.messagequeue.KafkaProducer;
import com.nayoung.itemservice.messagequeue.KafkaProducerConfig;
import com.nayoung.itemservice.messagequeue.client.OrderItemDto;
import com.nayoung.itemservice.messagequeue.client.OrderItemStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaProducerException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;

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
public class StockUpdateByKafkaStreams implements StockUpdate {

    private final ItemRepository itemRepository;
    private final ItemRedisRepository itemRedisRepository;
    private final KafkaProducer kafkaProducer;

    @Override
    public OrderItemDto updateStock(OrderItemDto orderItemDto, String eventId) {
        Item item = itemRepository.findById(orderItemDto.getItemId())
                .orElseThrow(() -> new ItemException(ExceptionCode.NOT_FOUND_ITEM));

        // Redis에서 재고 차감 시도
        if(isUpdatableStockByRedis(item.getId(), orderItemDto.getQuantity())) {
            orderItemDto.setOrderItemStatus((orderItemDto.getQuantity() < 0) ?
                    OrderItemStatus.SUCCEEDED  // consumption
                    : OrderItemStatus.CANCELED);  // production (undo)
        }
        else orderItemDto.setOrderItemStatus(OrderItemStatus.OUT_OF_STOCK);

        if(Objects.equals(OrderItemStatus.SUCCEEDED, orderItemDto.getOrderItemStatus())  // consumption
                || Objects.equals(OrderItemStatus.CANCELED, orderItemDto.getOrderItemStatus())) {  // production (undo)
            sendMessageToKafka(orderItemDto.getItemId(), orderItemDto.getQuantity());
        }

        // TODO: Redis 재고 변경 데이터 관리
        return orderItemDto;
    }

    private boolean isUpdatableStockByRedis(Long itemId, Long quantity) {
        Long stock = itemRedisRepository.incrementItemStock(itemId, quantity);
        if(stock >= 0) return true;

        // undo
        itemRedisRepository.decrementItemStock(itemId, quantity);
        return false;
    }

    private void sendMessageToKafka(Long itemId, Long quantity) {
        try {
            kafkaProducer.sendMessage(KafkaProducerConfig.ITEM_UPDATE_LOG_TOPIC, String.valueOf(itemId), quantity);
        } catch(KafkaProducerException e) {
            log.error("Kafka Exception " + e.getMessage());
            // TODO: broker에 적재되지 못한 이벤트 처리
        }
    }
}
