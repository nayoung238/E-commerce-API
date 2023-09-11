package com.nayoung.itemservice.domain.item;

import com.nayoung.itemservice.domain.item.log.ItemUpdateLog;
import com.nayoung.itemservice.domain.item.log.ItemUpdateLogRepository;
import com.nayoung.itemservice.domain.item.log.ItemUpdateStatus;
import com.nayoung.itemservice.exception.ItemException;
import com.nayoung.itemservice.messagequeue.KafkaConsumer;
import com.nayoung.itemservice.web.dto.ItemUpdateLogDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedissonItemService {

    private final RedissonClient redissonClient;
    private final String REDISSON_ITEM_LOCK_PREFIX = "ITEM:";
    private final ItemService itemService;
    private final ItemUpdateLogRepository itemUpdateLogRepository;
    private final OrderRedisRepository orderRedisRepository;

    @Transactional
    public void updateItemStockByOrderDetails(KafkaConsumer.OrderDetails orderDetails) {
        String[] redisKey = orderDetails.getCreatedAt().split(":");
        if(orderRedisRepository.addOrderId(redisKey[0], orderDetails.getOrderId()) == 1) {
            List<KafkaConsumer.ItemStockUpdateDetails> itemStockUpdateDetailsList = orderDetails.getItemStockUpdateDetailsList().stream()
                    .filter(itemStockUpdateDetails -> itemStockUpdateDetails.getQuantity() > 0L)
                    .map(i -> updateStock(orderDetails.getOrderId(), orderDetails.getCustomerAccountId(), i))
                    .collect(Collectors.toList());

            boolean isExistOutOfStockItem = itemStockUpdateDetailsList.stream()
                    .anyMatch(r -> Objects.equals(ItemUpdateStatus.OUT_OF_STOCK, r.getItemUpdateStatus()));

            if(isExistOutOfStockItem) undo(orderDetails.getOrderId());
        }
    }

    public KafkaConsumer.ItemStockUpdateDetails updateStock(Long orderId, Long customerAccountId, KafkaConsumer.ItemStockUpdateDetails itemStockUpdateDetails) {
        RLock lock = redissonClient.getLock(generateKey(itemStockUpdateDetails.getItemId()));
        try {
            boolean available = lock.tryLock(10, 1, TimeUnit.SECONDS);
            if(!available) {
                log.error("Lock 획득 실패");
                itemStockUpdateDetails.setItemUpdateStatus(ItemUpdateStatus.FAILED);
                return itemStockUpdateDetails;
            }
            ItemUpdateLogDto itemUpdateLogDto = itemService.updateStockByRedisson(orderId, customerAccountId, itemStockUpdateDetails);
            return KafkaConsumer.ItemStockUpdateDetails.fromItemUpdateLogDto(itemUpdateLogDto);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        } finally {
            lock.unlock();
        }
    }

    public String generateKey(Long key) {
        return REDISSON_ITEM_LOCK_PREFIX + key.toString();
    }

    private void undo(Long orderId) {
        List<ItemUpdateLog> itemUpdateLogs = itemUpdateLogRepository.findAllByOrderId(orderId);
        for(ItemUpdateLog itemUpdateLog : itemUpdateLogs) {
            if(Objects.equals(ItemUpdateStatus.SUCCEEDED, itemUpdateLog.getItemUpdateStatus())) {
                try {
                    updateStock(itemUpdateLog.getOrderId(),
                                itemUpdateLog.getCustomerAccountId(),
                                KafkaConsumer.ItemStockUpdateDetails.fromUndoItemUpdateLog(itemUpdateLog));
                } catch (ItemException e) {
                    log.error(e.getMessage());
                }
            }
        }
    }
}