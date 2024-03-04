package com.ecommerce.itemservice.domain.item.service;

import com.ecommerce.itemservice.exception.ExceptionCode;
import com.ecommerce.itemservice.exception.ItemException;
import com.ecommerce.itemservice.exception.OrderException;
import com.ecommerce.itemservice.exception.StockException;
import com.ecommerce.itemservice.kafka.dto.OrderItemDto;
import com.ecommerce.itemservice.kafka.dto.OrderItemStatus;
import com.ecommerce.itemservice.domain.item.dto.ItemDto;
import com.ecommerce.itemservice.domain.item.Item;
import com.ecommerce.itemservice.domain.item.repository.ItemRedisRepository;
import com.ecommerce.itemservice.domain.item.repository.ItemRepository;
import com.ecommerce.itemservice.domain.item.repository.OrderRedisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemService {

    private final ItemRepository itemRepository;
    private final ItemRedisRepository itemRedisRepository;
    private final OrderRedisRepository orderRedisRepository;

    public ItemDto create(ItemDto itemDto) {
        Item item = Item.fromItemDto(itemDto);
        item = itemRepository.save(item);

        itemRedisRepository.initializeItemStock(item.getId(), item.getStock());
        return ItemDto.fromItem(item);
    }

    @Transactional
    public OrderItemDto updateStockByPessimisticLock(OrderItemDto orderItemDto, String eventId) {
        try {
            Item item = itemRepository.findByIdWithPessimisticLock(orderItemDto.getItemId())
                    .orElseThrow(() -> new ItemException(ExceptionCode.NOT_FOUND_ITEM));

            item.updateStock(orderItemDto.getQuantity());

            orderItemDto.setOrderItemStatus((orderItemDto.getQuantity() < 0) ?
                    OrderItemStatus.SUCCEEDED  // consumption
                    : OrderItemStatus.CANCELED); // production (undo)
        } catch (ItemException e) {
            orderItemDto.setOrderItemStatus(OrderItemStatus.FAILED);
        } catch(StockException e) {
            orderItemDto.setOrderItemStatus(OrderItemStatus.OUT_OF_STOCK);
        }
        return orderItemDto;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public OrderItemDto updateStockByOptimisticLock(OrderItemDto orderItemDto, String eventId) {
        try {
            Item item = itemRepository.findByIdWithOptimisticLock(orderItemDto.getItemId())
                    .orElseThrow(() -> new ItemException(ExceptionCode.NOT_FOUND_ITEM));
            item.updateStock(orderItemDto.getQuantity());
            orderItemDto.setOrderItemStatus(OrderItemStatus.SUCCEEDED);
            itemRepository.save(item);
        } catch (ItemException e) {
            log.error(String.valueOf(e.getExceptionCode()));
            orderItemDto.setOrderItemStatus(OrderItemStatus.FAILED);
        } catch (StockException e) {
            log.error(String.valueOf(e.getExceptionCode()));
            orderItemDto.setOrderItemStatus(OrderItemStatus.OUT_OF_STOCK);
        } catch (ObjectOptimisticLockingFailureException e) {
            log.error(e.getMessage() + " -> Event Id: {}, Item Id: {}", eventId, orderItemDto.getItemId());
            orderItemDto.setOrderItemStatus(OrderItemStatus.FAILED);
        } catch (Exception e) {
            log.error(e.getMessage());
            orderItemDto.setOrderItemStatus(OrderItemStatus.FAILED);
        }
        return orderItemDto;
    }

    public OrderItemStatus findOrderProcessingStatus(String eventId) {
        String orderProcessingStatus = orderRedisRepository.getOrderStatus(eventId);
        if(orderProcessingStatus != null)
            return OrderItemStatus.getOrderItemStatus(orderProcessingStatus);
        else
            throw new OrderException(ExceptionCode.NOT_FOUND_ORDER_DETAILS);
    }
}