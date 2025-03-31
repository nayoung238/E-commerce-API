package com.ecommerce.itemservice.item.service;

import com.ecommerce.itemservice.item.entity.Item;
import com.ecommerce.itemservice.item.repository.ItemRedisRepository;
import com.ecommerce.itemservice.item.repository.ItemRepository;
import com.ecommerce.itemservice.item.enums.ItemProcessingStatus;
import com.ecommerce.itemservice.kafka.config.TopicConfig;
import com.ecommerce.itemservice.kafka.dto.OrderItemKafkaEvent;
import com.ecommerce.itemservice.kafka.dto.OrderStatus;
import com.ecommerce.itemservice.kafka.service.KafkaProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.kafka.core.KafkaProducerException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Kafka Streams 이용해 이벤트 집계 후 DB에 반영하는 방식
 * -> 재고 변경 데이터를 event로 생성 및 Kafka Streams로 이벤트 집계 후 DB 반영
 *
 * Late event까지 고려하는 적절한 Grace Period를 설정할 수 없음
 * -> 네트워크 지연 예측 불가
 * -> 정확한 집계 불가능 (더 이상 해당 방식을 사용하지 않음)
 */

@Service //@Primary
@RequiredArgsConstructor
@Slf4j
public class StockUpdateByKafkaStreamsServiceImpl implements StockUpdateService {

    private final ItemRepository itemRepository;
    private final ItemRedisRepository itemRedisRepository;
    private final KafkaProducerService kafkaProducerService;

    @Override
    @Transactional
    public OrderItemKafkaEvent updateStock(OrderItemKafkaEvent orderItemKafkaEvent, ItemProcessingStatus itemProcessingStatus) {
        Optional<Item> item = itemRepository.findById(orderItemKafkaEvent.getItemId());
        if(item.isPresent()) {
            // Redis에서 재고 차감 시도
            ItemProcessingStatus updateStatus = isStockUpdatableInRedis(item.get().getId(), orderItemKafkaEvent.getQuantity(), itemProcessingStatus);
            if(updateStatus == ItemProcessingStatus.UPDATE_SUCCESSFUL) {
                if(itemProcessingStatus == ItemProcessingStatus.STOCK_CONSUMPTION) {
                    orderItemKafkaEvent.updateOrderStatus(OrderStatus.SUCCESSFUL);
                }
                else if(itemProcessingStatus == ItemProcessingStatus.STOCK_PRODUCTION) {
                    orderItemKafkaEvent.updateOrderStatus(OrderStatus.CANCELED);
                }
                sendMessageToKafka(orderItemKafkaEvent.getItemId(), orderItemKafkaEvent.getQuantity(), itemProcessingStatus);
            } else {
                orderItemKafkaEvent.updateOrderStatus(updateStatus);
            }
            return orderItemKafkaEvent;
        }
        else {
            orderItemKafkaEvent.updateOrderStatus(ItemProcessingStatus.ITEM_NOT_FOUND);
            return orderItemKafkaEvent;
        }
    }

    private ItemProcessingStatus isStockUpdatableInRedis(Long itemId, Long quantity, ItemProcessingStatus itemProcessingStatus) {
        ItemProcessingStatus updateStatus = updateStockInRedis(itemId, quantity, itemProcessingStatus);
        if ((itemProcessingStatus == ItemProcessingStatus.STOCK_CONSUMPTION && updateStatus == ItemProcessingStatus.SUCCESSFUL_CONSUMPTION)
                || (itemProcessingStatus == ItemProcessingStatus.STOCK_PRODUCTION && updateStatus == ItemProcessingStatus.STOCK_PRODUCTION)) return ItemProcessingStatus.UPDATE_SUCCESSFUL;

        return updateStatus;
    }

    private ItemProcessingStatus updateStockInRedis(Long itemId, Long quantity, ItemProcessingStatus itemProcessingStatus) {
        try {
            if (itemProcessingStatus == ItemProcessingStatus.STOCK_CONSUMPTION) {
                Long stock = itemRedisRepository.decrementItemStock(itemId, quantity);
                if (stock >= 0) {
                    return ItemProcessingStatus.SUCCESSFUL_CONSUMPTION;
                }
                itemRedisRepository.incrementItemStock(itemId, quantity);
                return ItemProcessingStatus.OUT_OF_STOCK;
            }
            else if (itemProcessingStatus == ItemProcessingStatus.STOCK_PRODUCTION) {
                itemRedisRepository.incrementItemStock(itemId, quantity);
                return ItemProcessingStatus.SUCCESSFUL_PRODUCTION;
            }
            return ItemProcessingStatus.UPDATE_FAILED;
        } catch (InvalidDataAccessApiUsageException e) {
            log.error("{} (ItemId: {})", e.getMessage(), itemId);
            return ItemProcessingStatus.ITEM_NOT_FOUND;
        }
    }

    private void sendMessageToKafka(Long itemId, Long quantity, ItemProcessingStatus itemProcessingStatus) {
        try {
            if(itemProcessingStatus == ItemProcessingStatus.STOCK_CONSUMPTION) {
                quantity *= -1;
            }
            kafkaProducerService.sendMessage(TopicConfig.ITEM_UPDATE_LOG_TOPIC, String.valueOf(itemId), quantity);
        } catch(KafkaProducerException e) {
            log.error("Kafka Exception " + e.getMessage());
            // TODO: broker에 적재되지 못한 이벤트 처리
        }
    }
}
