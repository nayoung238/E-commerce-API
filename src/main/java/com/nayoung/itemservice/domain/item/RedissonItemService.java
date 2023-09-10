package com.nayoung.itemservice.domain.item;

import com.nayoung.itemservice.domain.item.log.ItemUpdateLog;
import com.nayoung.itemservice.domain.item.log.ItemUpdateLogRepository;
import com.nayoung.itemservice.domain.item.log.ItemUpdateStatus;
import com.nayoung.itemservice.exception.ItemException;
import com.nayoung.itemservice.messagequeue.KafkaConsumer;
import com.nayoung.itemservice.web.dto.ItemStockUpdateDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public KafkaConsumer.OrderDetails updateItemStockByOrderDetails(KafkaConsumer.OrderDetails orderDetails) {
        List<ItemStockUpdateDto> result = orderDetails.getItemStockUpdateDtos().stream()
                .map(i -> updateStock(orderDetails.getOrderId(), orderDetails.getCustomerAccountId(), i))
                .collect(Collectors.toList());

        boolean isExistOutOfStockItem = result.stream()
                .anyMatch(r -> Objects.equals(ItemUpdateStatus.OUT_OF_STOCK, r.getItemUpdateStatus()));

        if(isExistOutOfStockItem) {
            undo(orderDetails.getOrderId());
            for(ItemStockUpdateDto itemStockUpdateDto : result)
                itemStockUpdateDto.setItemUpdateStatus(ItemUpdateStatus.FAILED);
        }

        return KafkaConsumer.OrderDetails.builder()
                .orderId(orderDetails.getOrderId())
                .customerAccountId(orderDetails.getCustomerAccountId())
                .createdAt(orderDetails.getCreatedAt())
                .itemStockUpdateDtos(result)
                .build();
    }

    public ItemStockUpdateDto updateStock(Long orderId, Long customerAccountId, ItemStockUpdateDto itemStockUpdateDto) {
        RLock lock = redissonClient.getLock(generateKey(itemStockUpdateDto.getItemId()));
        try {
            boolean available = lock.tryLock(10, 1, TimeUnit.SECONDS);
            if(!available) {
                log.error("Lock 획득 실패");
                return ItemStockUpdateDto.builder()
                        .itemUpdateStatus(ItemUpdateStatus.FAILED)
                        .shopId(itemStockUpdateDto.getShopId())
                        .itemId(itemStockUpdateDto.getItemId())
                        .quantity(itemStockUpdateDto.getQuantity())
                        .build();
            }
            return itemService.updateStockByRedisson(orderId, customerAccountId, itemStockUpdateDto);
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
            if(Objects.equals(ItemUpdateStatus.SUCCEED, itemUpdateLog.getItemUpdateStatus())) {
                try {
                    updateStock(itemUpdateLog.getOrderId(),
                                itemUpdateLog.getCustomerAccountId(),
                                ItemStockUpdateDto.fromItemUpdateLog(itemUpdateLog));
                } catch (ItemException e) {
                    log.error(e.getMessage());
                }
            }
        }
    }
}