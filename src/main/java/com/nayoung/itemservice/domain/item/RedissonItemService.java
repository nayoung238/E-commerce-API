package com.nayoung.itemservice.domain.item;

import com.nayoung.itemservice.domain.item.log.ItemUpdateLog;
import com.nayoung.itemservice.domain.item.log.ItemUpdateLogRepository;
import com.nayoung.itemservice.domain.item.log.ItemUpdateStatus;
import com.nayoung.itemservice.exception.ItemException;
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

@Slf4j
@Component
@RequiredArgsConstructor
public class RedissonItemService {

    private final RedissonClient redissonClient;
    private final ItemService itemService;
    private final ItemUpdateLogRepository itemUpdateLogRepository;

    public ItemStockUpdateDto updateStock(ItemStockUpdateDto request) {
        RLock lock = redissonClient.getLock(generateKey(request.getItemId()));
        ItemStockUpdateDto itemStockUpdateDto = null;
        try {
            boolean available = lock.tryLock(10, 1, TimeUnit.SECONDS);
            if(!available) {
                log.error("Lock 획득 실패");
                return ItemStockUpdateDto.fromOrderItemRequest(ItemUpdateStatus.FAILED, request);
            }
            itemStockUpdateDto = itemService.updateStockByRedisson(request);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
        return itemStockUpdateDto;
    }

    @Transactional
    public void undo(Long orderId) {
        List<ItemUpdateLog> itemUpdateLogs = itemUpdateLogRepository.findAllByOrderId(orderId);
        for(ItemUpdateLog itemUpdateLog : itemUpdateLogs) {
            if(Objects.equals(ItemUpdateStatus.SUCCEED, itemUpdateLog.getItemUpdateStatus())) {
                try {
                    updateStock(ItemStockUpdateDto.fromItemUpdateLog(ItemUpdateStatus.CANCELED, itemUpdateLog));
                } catch (ItemException e) {
                    log.error(e.getMessage());
                }
            }
        }
    }

    public String generateKey(Long key) {
        return key.toString();
    }
}