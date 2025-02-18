package com.ecommerce.itemservice.domain.item.service;

import com.ecommerce.itemservice.domain.item.ItemProcessingStatus;
import com.ecommerce.itemservice.kafka.dto.OrderItemKafkaEvent;
import com.ecommerce.itemservice.kafka.dto.OrderProcessingStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;
import java.util.concurrent.TimeUnit;

@Service @Primary
@RequiredArgsConstructor
@Slf4j
public class StockUpdateByRedissonServiceImpl implements StockUpdateService {

    private final RedissonClient redissonClient;
    private final ItemService itemService;

    private final Long RLOCK_WAIT_TIME = 10000L;
    private final Long RLOCK_LEASE_TIME = 3000L;
    private final String REDISSON_ITEM_LOCK_PREFIX = "ITEM:";

    @Override
    public OrderItemKafkaEvent updateStock(OrderItemKafkaEvent orderItemKafkaEvent, ItemProcessingStatus itemProcessingStatus) {
        RLock lock = redissonClient.getLock(generateKey(orderItemKafkaEvent.getItemId()));
        try {
            boolean available = lock.tryLock(RLOCK_WAIT_TIME, RLOCK_LEASE_TIME, TimeUnit.MILLISECONDS);
            if(available) {
                log.info("Acquired the RLock -> Redisson Lock: {}", lock.getName());
                // Transaction Propagation.REQUIRES_NEW
                return itemService.updateStockByOptimisticLock(orderItemKafkaEvent, itemProcessingStatus);
            }
            else {
                orderItemKafkaEvent.updateOrderProcessingStatus(OrderProcessingStatus.FAILED);
                return orderItemKafkaEvent;
            }
        } catch (InterruptedException e) {
            log.error("Thread was interrupted while trying to acquire the Redisson lock for item ID {}: {}", orderItemKafkaEvent.getItemId(), e.getMessage());
            orderItemKafkaEvent.updateOrderProcessingStatus(OrderProcessingStatus.FAILED);
        } catch (CannotCreateTransactionException e) {
            log.error("Failed to create a new transaction (internal transaction) while updating stock for item ID {}: {}", orderItemKafkaEvent.getItemId(), e.getMessage());
            orderItemKafkaEvent.updateOrderProcessingStatus(OrderProcessingStatus.FAILED);
        } finally {
            if(lock.isHeldByCurrentThread()) {
                log.info("Unlock -> Redisson Lock: {}", lock.getName());
                lock.unlock();
            }
        }
        return orderItemKafkaEvent;
    }

    private String generateKey(Long key) {
        return REDISSON_ITEM_LOCK_PREFIX + key.toString();
    }
}
