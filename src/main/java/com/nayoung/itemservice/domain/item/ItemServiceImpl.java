package com.nayoung.itemservice.domain.item;

import com.nayoung.itemservice.domain.discount.DiscountCode;
import com.nayoung.itemservice.domain.item.log.ItemUpdateLog;
import com.nayoung.itemservice.domain.item.log.ItemUpdateLogRepository;
import com.nayoung.itemservice.domain.item.log.OrderStatus;
import com.nayoung.itemservice.domain.shop.Shop;
import com.nayoung.itemservice.domain.shop.ShopService;
import com.nayoung.itemservice.exception.ExceptionCode;
import com.nayoung.itemservice.exception.ItemException;
import com.nayoung.itemservice.exception.StockException;
import com.nayoung.itemservice.web.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final ShopService shopService;
    private final ItemUpdateLogRepository itemUpdateLogRepository;

    @Override
    public ItemResponse createItem(ItemCreationRequest request) {
        Shop shop = shopService.findShopById(request.getShopId());
        Item item = Item.fromItemCreationRequestAndShopEntity(request, shop);
        Item savedItem = itemRepository.save(item);
        return ItemResponse.fromItemEntity(savedItem);
    }

    @Override
    public ItemResponse findItemByItemId(ItemInfoByItemIdRequest request) {
        DiscountCode customerDiscountCode = DiscountCode.getDiscountCode(request.getCustomerRating());
        Item item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new ItemException(ExceptionCode.NOT_FOUND_ITEM));

        return DiscountCode.applyDiscountByItemEntity(item, customerDiscountCode);
    }

    @Override
    public List<ItemResponse> findItemsByItemName(ItemInfoByShopLocationRequest request) {
        DiscountCode customerDiscountCode = DiscountCode.getDiscountCode(request.getCustomerRating());
        List<Shop> shops = shopService.findShops(request.getProvince(), request.getCity());

        if (shops.isEmpty()) // 원하는 지역에 상점이 존재하지 않거나, 원하는 상점이 없는 경우
            return getItemsByNameAsync(request.getItemName(), customerDiscountCode);
        else // 원하는 지역에 상점이 존재하는 경우
            return getItemsByShopAsync(shops, request.getItemName(), customerDiscountCode);
    }

    /**
     * 원하는 지역에 상점이 존재하는 경우
     * 해당되는 상점에서 아이템을 찾아 할인 적용
     * 아이템이 없다면 '해당 상점에는 아이템이 존재하지 않다'는 상태 반환
     */
    private List<ItemResponse> getItemsByShopAsync(List<Shop> shops, String itemName, DiscountCode customerDiscountCode) {
        List<CompletableFuture<ItemResponse>> itemResponses = shops.stream()
                .map(shop -> CompletableFuture.supplyAsync(
                        () -> itemRepository.findByShopAndName(shop, itemName)))
                .map(future -> future.thenApply(ItemDto::getInstance))
                .map(future -> future.thenCompose(itemDto ->
                        CompletableFuture.supplyAsync(
                                () -> DiscountCode.applyDiscountByItemDto(itemDto, customerDiscountCode))))
                .collect(Collectors.toList());

        return itemResponses.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
    }

    /**
     * 원하는 지역에 상점이 존재하지 않거나, 원하는 상점이 없는 경우
     * 모든 지역을 기준으로 아이템을 가지고 있는 상점을 찾아 할인 적용
     */
    private List<ItemResponse> getItemsByNameAsync(String itemName, DiscountCode customerDiscountCode) {
        List<Item> items = itemRepository.findAllByName(itemName);
        if(items.isEmpty())
            throw new ItemException(ExceptionCode.NOT_FOUND_ITEM);

        List<CompletableFuture<ItemResponse>> itemResponses = items.stream()
                .map(item -> CompletableFuture.supplyAsync(
                        () -> DiscountCode.applyDiscountByItemEntity(item, customerDiscountCode)))
                .collect(Collectors.toList());

        return itemResponses.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ItemResponse update(ItemInfoUpdateRequest itemInfoUpdateRequest) {
        Item item = itemRepository.findByIdWithPessimisticLock(itemInfoUpdateRequest.getItemId()).orElseThrow();
        item.update(itemInfoUpdateRequest);
        return ItemResponse.fromItemEntity(item);
    }

    @Override
    @Transactional
    public OrderItemResponse decreaseStock(Long orderId, OrderItemRequest request) {
        boolean isSuccess = false;
        try {
            Item item = itemRepository.findByIdWithPessimisticLock(request.getItemId())
                    .orElseThrow(() -> new ItemException(ExceptionCode.NOT_FOUND_ITEM));
            item.decreaseStock(request.getQuantity());

            isSuccess = true;
            ItemUpdateLog itemUpdateLog = ItemUpdateLog.from(OrderStatus.SUCCEED, orderId, request);
            itemUpdateLogRepository.save(itemUpdateLog);
        } catch (ItemException | StockException e) {
            isSuccess = false;
            ItemUpdateLog itemUpdateLog = ItemUpdateLog.from(OrderStatus.FAILED, orderId, request);
            itemUpdateLogRepository.save(itemUpdateLog);
        }
        if(isSuccess)
            return OrderItemResponse.fromOrderItemRequest(OrderStatus.SUCCEED, request);
        return OrderItemResponse.fromOrderItemRequest(OrderStatus.FAILED, request);
    }

    @Override
    @Transactional
    public void undo(Long orderId, List<OrderItemResponse> orderItemResponses) {
        increaseStockByOrderId(orderId);
        for(OrderItemResponse orderItemResponse : orderItemResponses)
            orderItemResponse.setOrderStatus(OrderStatus.FAILED);
    }

    public void increaseStockByOrderId(Long orderId) {
        List<ItemUpdateLog> itemUpdateLogs = itemUpdateLogRepository.findAllByOrderId(orderId);
        for(ItemUpdateLog itemUpdateLog : itemUpdateLogs) {
            if(itemUpdateLog.getOrderStatus() == OrderStatus.SUCCEED) {
                try {
                    increaseStock(itemUpdateLog.getItemId(), itemUpdateLog.getQuantity());
                    itemUpdateLog.setOrderStatus(OrderStatus.CANCELED);
                } catch (ItemException e) {
                    log.error(e.getMessage());
                }
            }
        }
    }

    public void increaseStock(Long itemId, Long quantity) {
        Item item = itemRepository.findByIdWithPessimisticLock(itemId)
                .orElseThrow(() -> new ItemException(ExceptionCode.NOT_FOUND_ITEM));

        item.increaseStock(quantity);
    }
}