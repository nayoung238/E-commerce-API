package com.nayoung.itemservice.domain.item;

import com.nayoung.itemservice.domain.item.log.ItemUpdateLog;
import com.nayoung.itemservice.domain.item.log.ItemUpdateLogRepository;
import com.nayoung.itemservice.domain.item.log.ItemUpdateStatus;
import com.nayoung.itemservice.exception.ExceptionCode;
import com.nayoung.itemservice.exception.ItemException;
import com.nayoung.itemservice.exception.StockException;
import com.nayoung.itemservice.messagequeue.KafkaConsumer;
import com.nayoung.itemservice.web.dto.ItemUpdateLogDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ItemStockService {

    private final ItemRepository itemRepository;
    private final ItemRedisRepository itemRedisRepository;
    private final ItemUpdateLogRepository itemUpdateLogRepository;

    @Transactional
    public ItemUpdateLogDto decreaseStockByPessimisticLock(Long orderId, Long customerAccountId, KafkaConsumer.ItemStockUpdateDetails request) {
        ItemUpdateStatus itemUpdateStatus;
        try {
            Item item = itemRepository.findByIdWithPessimisticLock(request.getItemId())
                    .orElseThrow(() -> new ItemException(ExceptionCode.NOT_FOUND_ITEM));
            item.decreaseStock(request.getQuantity());
            itemUpdateStatus = ItemUpdateStatus.SUCCEEDED;
        } catch (ItemException e) {
            itemUpdateStatus = ItemUpdateStatus.FAILED;
        } catch(StockException e) {
            itemUpdateStatus = ItemUpdateStatus.OUT_OF_STOCK;
        }
        ItemUpdateLog itemUpdateLog = ItemUpdateLog.from(itemUpdateStatus, orderId, customerAccountId, request);
        itemUpdateLogRepository.save(itemUpdateLog);
        return ItemUpdateLogDto.fromItemUpdateLog(itemUpdateLog);
    }

    public ItemUpdateLogDto updateStockByRedisson(Long orderId, Long customerAccountId, KafkaConsumer.ItemStockUpdateDetails request) {
        Item item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new ItemException(ExceptionCode.NOT_FOUND_ITEM));

        ItemUpdateStatus itemUpdateStatus;
        if(updateStockByRedis(item.getId(), request.getQuantity())) itemUpdateStatus = (request.getQuantity() >= 0) ? ItemUpdateStatus.SUCCEEDED : ItemUpdateStatus.CANCELED;
        else itemUpdateStatus = ItemUpdateStatus.OUT_OF_STOCK;

        ItemUpdateLog itemUpdateLog = ItemUpdateLog.from(itemUpdateStatus, orderId, customerAccountId, request);
        itemUpdateLogRepository.save(itemUpdateLog);
        return ItemUpdateLogDto.fromItemUpdateLog(itemUpdateLog);
    }

    private boolean updateStockByRedis(Long itemId, Long quantity) {
        Long stock = itemRedisRepository.decrementItemStock(itemId, quantity);
        if(stock >= 0) return true;

        itemRedisRepository.incrementItemStock(itemId, quantity);
        return false;
    }
}
