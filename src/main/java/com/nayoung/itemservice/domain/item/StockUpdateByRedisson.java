package com.nayoung.itemservice.domain.item;

import com.nayoung.itemservice.domain.item.log.ItemUpdateLog;
import com.nayoung.itemservice.domain.item.log.ItemUpdateLogRepository;
import com.nayoung.itemservice.exception.ExceptionCode;
import com.nayoung.itemservice.exception.ItemException;
import com.nayoung.itemservice.messagequeue.client.OrderItemDto;
import com.nayoung.itemservice.messagequeue.client.OrderItemStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockUpdateByRedisson implements StockUpdate {

    private final ItemRepository itemRepository;
    private final ItemUpdateLogRepository itemUpdateLogRepository;
    private final ItemRedisRepository itemRedisRepository;
    private final RedissonClient redissonClient;
    private final String REDISSON_ITEM_LOCK_PREFIX = "ITEM:";

    /**
     * Redis Distributed Lock + Optimistic Lock 사용
     * Redis 분산락을 획득한 요청만이 DB에 접근해 수정할 수 있음
     * 분산락에 lease time 설정 -> DB 락을 획득한 노드가 죽는 경우 발생할 수 있는 문제 해결 (활성 상태가 될 때까지 모든 요청 대기해야 하는 문제)
     *
     * 분산락 lease time 보다 transaction 처리가 더 길다면 동시성 문제 발생할 수 있음 (여러 요청이 자신이 분산락 주인이라고 착각하고 쿼리 날리는 경우)
     * -> Optimistic Lock을 사용해 DB 반영 시 충돌 감지해 동시성 문제 해결
     */
    @Override
    @Transactional
    public OrderItemDto updateStock(OrderItemDto orderItemDto, String eventId) {
        RLock lock = redissonClient.getLock(generateKey(orderItemDto.getItemId()));
        try {
            boolean available = lock.tryLock(10, 1, TimeUnit.SECONDS);
            if(!available) {
                log.error("Lock 획득 실패");
                orderItemDto.setOrderItemStatus(OrderItemStatus.FAILED);
            }
            return updateStockByRedisson(orderItemDto, eventId);
        } catch (InterruptedException e) {
            e.printStackTrace();
            orderItemDto.setOrderItemStatus(OrderItemStatus.FAILED);
        } finally {
            lock.unlock();
        }
        return orderItemDto;
    }

    private String generateKey(Long key) {
        return REDISSON_ITEM_LOCK_PREFIX + key.toString();
    }

    private OrderItemDto updateStockByRedisson(OrderItemDto orderItemDto, String eventId) {
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

        ItemUpdateLog itemUpdateLog = ItemUpdateLog.from(orderItemStatus, orderItemDto, eventId);
        itemUpdateLogRepository.save(itemUpdateLog);

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
}
