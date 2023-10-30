package com.nayoung.itemservice.domain.item;

import com.nayoung.itemservice.domain.item.log.ItemUpdateLog;
import com.nayoung.itemservice.domain.item.log.ItemUpdateLogRepository;
import com.nayoung.itemservice.exception.ExceptionCode;
import com.nayoung.itemservice.exception.ItemException;
import com.nayoung.itemservice.messagequeue.KafkaProducer;
import com.nayoung.itemservice.messagequeue.KafkaProducerConfig;
import com.nayoung.itemservice.messagequeue.client.OrderItemDto;
import com.nayoung.itemservice.messagequeue.client.OrderItemStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.KafkaProducerException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

@Service @Primary
@RequiredArgsConstructor
@Slf4j
public class StockUpdateByKStream implements StockUpdate {

    private final ItemRepository itemRepository;
    private final ItemUpdateLogRepository itemUpdateLogRepository;
    private final ItemRedisRepository itemRedisRepository;
    private final KafkaProducer kafkaProducer;

    /**
     * Kafka KStream을 이용해 집계하는 방식
     * 재고 변경 로그를 KStream(Key: Item ID, Value: Quantity)에 추가
     * groupByKey로 합계를 구해 DB에 반영 (여러 변경 요청을 모아 한 번에 DB에 반영)
     * 집계하는 과정에서 이벤트가 중복되지 않게 Tumbling window 사용
     */
    @Override
    public OrderItemDto updateStock(OrderItemDto orderItemDto, String eventId) {
        Item item = itemRepository.findById(orderItemDto.getItemId())
                .orElseThrow(() -> new ItemException(ExceptionCode.NOT_FOUND_ITEM));

        // Redis에서 재고 차감 시도
        OrderItemStatus orderItemStatus;
        if(isUpdatableStockByRedis(item.getId(), orderItemDto.getQuantity())) {
            orderItemStatus = (orderItemDto.getQuantity() < 0) ?
                    OrderItemStatus.SUCCEEDED  // consumption
                    : OrderItemStatus.CANCELED;  // undo 작업에서 발생하는 production
        }
        else orderItemStatus = OrderItemStatus.OUT_OF_STOCK;

        // undo 작업 판별하기 위해 DB에 기록
        ItemUpdateLog itemUpdateLog = ItemUpdateLog.from(orderItemStatus, orderItemDto, eventId);
        itemUpdateLogRepository.save(itemUpdateLog);

        /*
            consumption(SUCCEEDED)이거나 UNDO 작업에서 발생하는 production(CANCELED)이면 재고가 변경되어야 하므로
            KStream에 재고 변경 데이터 추가
         */
        if(Objects.equals(OrderItemStatus.SUCCEEDED, itemUpdateLog.getOrderItemStatus())
                || Objects.equals(OrderItemStatus.CANCELED, itemUpdateLog.getOrderItemStatus())) {
            final Long itemUpdateLogId = itemUpdateLog.getId();
            sendMessageToKStream(orderItemDto.getItemId(), orderItemDto.getQuantity(), itemUpdateLogId);
        }

        orderItemDto.setOrderItemStatus(orderItemStatus);
        return orderItemDto;
    }

    private boolean isUpdatableStockByRedis(Long itemId, Long quantity) {
        Long stock = itemRedisRepository.incrementItemStock(itemId, quantity);
        if(stock >= 0) return true;

        // undo
        itemRedisRepository.decrementItemStock(itemId, quantity);
        return false;
    }

    private void sendMessageToKStream(Long itemId, Long quantity, Long itemUpdateLogId) {
        try {
            kafkaProducer.sendMessage(KafkaProducerConfig.ITEM_UPDATE_LOG_TOPIC, String.valueOf(itemId), quantity);
            setLogCreatedAt(itemUpdateLogId);  // broker에 log 적재한 후의 시간 기록
        } catch(KafkaProducerException e) {
            log.error("Kafka Exception " + e.getMessage());
            // TODO: broker에 적재되지 못하면 logCreatedAt 값이 null -> null 값만 batch 처리
        }
    }

    private void setLogCreatedAt(Long id) {
        ItemUpdateLog itemUpdateLog = itemUpdateLogRepository.findById(id).orElseThrow();
        itemUpdateLog.setLogCreatedAt(LocalDateTime.now());
        itemUpdateLogRepository.save(itemUpdateLog);
    }
}
