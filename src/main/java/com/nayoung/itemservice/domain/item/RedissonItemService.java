package com.nayoung.itemservice.domain.item;

import com.nayoung.itemservice.domain.item.log.OrderStatus;
import com.nayoung.itemservice.web.dto.ItemStockToUpdateDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedissonItemService {

    private final RedissonClient redissonClient;
    private final ItemService itemService;

    public ItemStockToUpdateDto decreaseItemStock(ItemStockToUpdateDto request) {
        RLock lock = redissonClient.getLock(generateKey(request.getItemId()));
        ItemStockToUpdateDto itemStockToUpdateDto = null;
        try {
            boolean available = lock.tryLock(10, 1, TimeUnit.SECONDS);
            if(!available) {
                log.error("Lock 획득 실패");
                return ItemStockToUpdateDto.fromOrderItemRequest(OrderStatus.FAILED, request);
            }
            itemStockToUpdateDto = itemService.decreaseStockByRedisson(request);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
        return itemStockToUpdateDto;
    }

    public void undo(Long orderId) {
        itemService.undo(orderId);
    }

    public String generateKey(Long key) {
        return key.toString();
    }
}