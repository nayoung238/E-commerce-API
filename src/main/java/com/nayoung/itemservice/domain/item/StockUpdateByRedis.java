package com.nayoung.itemservice.domain.item;

import com.nayoung.itemservice.exception.ExceptionCode;
import com.nayoung.itemservice.exception.ItemException;
import com.nayoung.itemservice.messagequeue.client.OrderItemDto;
import com.nayoung.itemservice.messagequeue.client.OrderItemStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Redis에서 재고 데이터 관리하는 방식
 * -> DB Lock을 획득하지 않아도 됨
 *    DB Lock 획득하기 위한 대기 시간 발생하지 않음
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class StockUpdateByRedis implements StockUpdate {

    private final ItemRepository itemRepository;
    private final ItemRedisRepository itemRedisRepository;

    @Override
    public OrderItemDto updateStock(OrderItemDto orderItemDto, String eventId) {
        OrderItemDto result = updateStockInRedis(orderItemDto);
        // TODO: Redis 재고 변경 데이터 관리
        return result;
    }

    private OrderItemDto updateStockInRedis(OrderItemDto orderItemDto) {
        Item item = itemRepository.findById(orderItemDto.getItemId())
                .orElseThrow(() -> new ItemException(ExceptionCode.NOT_FOUND_ITEM));

        // Redis에서 재고 차감 시도
        if(isUpdatableStockByRedis(item.getId(), orderItemDto.getQuantity())) {
            orderItemDto.setOrderItemStatus((orderItemDto.getQuantity() < 0) ?
                    OrderItemStatus.SUCCEEDED  // consumption
                    : OrderItemStatus.CANCELED);  // undo 작업에서 발생하는 production
        }
        else orderItemDto.setOrderItemStatus(OrderItemStatus.OUT_OF_STOCK);
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