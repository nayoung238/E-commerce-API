package com.nayoung.itemservice.domain.item;

import com.nayoung.itemservice.domain.discount.DiscountCode;
import com.nayoung.itemservice.domain.item.log.ItemUpdateLog;
import com.nayoung.itemservice.domain.item.log.ItemUpdateLogRepository;
import com.nayoung.itemservice.domain.item.log.ItemUpdateStatus;
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
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemService {

    private final ItemRepository itemRepository;
    private final ShopService shopService;
    private final ItemUpdateLogRepository itemUpdateLogRepository;
    private final ItemRedisRepository itemRedisRepository;

    public ItemDto create(ItemDto itemDto) {
        Shop shop = shopService.findShopById(itemDto.getShopId());
        Item item = Item.fromItemDtoAndShopEntity(itemDto, shop);
        item = itemRepository.save(item);

        itemRedisRepository.initializeItemStock(item.getId(), item.getStock());
        return ItemDto.fromItem(item);
    }

    public ItemDto findItemByItemId(Long itemId, String customerRating) {
        DiscountCode customerDiscountCode = DiscountCode.getDiscountCode(customerRating);
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemException(ExceptionCode.NOT_FOUND_ITEM));

        return DiscountCode.applyDiscountByItem(item, customerDiscountCode);
    }

    public List<ItemDto> findItems(String itemName, String location, String customerRating) {
        if(!StringUtils.hasText(location)) location = "none";
        List<Shop> shops = shopService.findAllShopByLocation(location);

        if(!StringUtils.hasText(customerRating)) customerRating = "UNQUALIFIE";
        DiscountCode customerDiscountCode = DiscountCode.getDiscountCode(customerRating);

        if (shops.isEmpty()) // 원하는 지역에 상점이 존재하지 않거나, 원하는 상점이 없는 경우
            return findItemsByName(itemName, customerDiscountCode);
        else // 원하는 지역에 상점이 존재하는 경우
            return findItemsByShop(shops, itemName, customerDiscountCode);
    }

    private List<ItemDto> findItemsByShop(List<Shop> shops, String itemName, DiscountCode discountCode) {
        List<ItemDto> itemDtos = shops.stream()
                .map(shop -> itemRepository.findByShopAndName(shop, itemName))
                .filter(Optional::isPresent)
                .map(item -> ItemDto.fromItem(item.get()))
                .map(itemDto -> DiscountCode.applyDiscountByItemDto(itemDto, discountCode))
                .collect(Collectors.toList());

        if(itemDtos.isEmpty()) {
            return findItemsByName(itemName, discountCode);
        }
        return itemDtos;
    }

    private List<ItemDto> findItemsByName(String itemName, DiscountCode discountCode) {
        List<Item> items = itemRepository.findAllByName(itemName);
        if(items.isEmpty()) {
            throw new ItemException(ExceptionCode.NOT_FOUND_ITEM);
        }
        return items.stream()
                .map(ItemDto::fromItem)
                .map(i -> DiscountCode.applyDiscountByItemDto(i, discountCode))
                .collect(Collectors.toList());
    }

    public List<ItemDto> findItemsAsync (String itemName, String location, String customerRating) {
        if(!StringUtils.hasText(location)) location = "none";
        List<Shop> shops = shopService.findAllShopByLocation(location);

        if(!StringUtils.hasText(customerRating)) customerRating = "UNQUALIFIE";
        DiscountCode customerDiscountCode = DiscountCode.getDiscountCode(customerRating);

        if (shops.isEmpty()) // 원하는 지역에 상점이 존재하지 않거나, 원하는 상점이 없는 경우
            return findItemsByNameAsync(itemName, customerDiscountCode);
        else // 원하는 지역에 상점이 존재하는 경우
            return findItemsByShopAsync(shops, itemName, customerDiscountCode);
    }

    /**
     * 원하는 지역에 상점이 존재하는 경우
     * 해당되는 상점에서 아이템을 찾아 할인 적용
     */
    private List<ItemDto> findItemsByShopAsync(List<Shop> shops, String itemName, DiscountCode discountCode) {
        List<CompletableFuture<ItemDto>> itemDtos = shops.stream()
                .map(shop -> CompletableFuture.supplyAsync(
                        () -> itemRepository.findByShopAndName(shop, itemName)))
                .map(future -> future.thenApply(ItemDto::getInstance))
                .map(future -> future.thenCompose(itemDto ->
                        CompletableFuture.supplyAsync(
                                () -> DiscountCode.applyDiscountByItemDto(itemDto, discountCode))))
                .collect(Collectors.toList());

        if(itemDtos.isEmpty()) return findItemsByNameAsync(itemName, discountCode);
        return itemDtos.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
    }

    /**
     * 원하는 지역에 상점이 존재하지 않거나, 원하는 상점이 없는 경우
     * 원하는 지역에 상점이 있지만, 상품이 없는 경우
     * 모든 지역을 기준으로 아이템을 가지고 있는 상점을 찾아 할인 적용
     */
    private List<ItemDto> findItemsByNameAsync(String itemName, DiscountCode discountCode) {
        List<Item> items = itemRepository.findAllByName(itemName);
        if(items.isEmpty())
            throw new ItemException(ExceptionCode.NOT_FOUND_ITEM);

        List<CompletableFuture<ItemDto>> itemResponses = items.stream()
                .map(item -> CompletableFuture.supplyAsync(
                        () -> DiscountCode.applyDiscountByItem(item, discountCode)))
                .collect(Collectors.toList());

        return itemResponses.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
    }

    @Transactional
    public ItemDto update(ItemInfoUpdateRequest itemInfoUpdateRequest) {
        Item item = itemRepository.findByIdWithPessimisticLock(itemInfoUpdateRequest.getItemId()).orElseThrow();
        item.update(itemInfoUpdateRequest);
        return ItemDto.fromItem(item);
    }

    public ItemStockUpdateDto updateStockByRedisson(Long orderId, Long customerAccountId, ItemStockUpdateDto request) {
        Item item = itemRepository.findById(request.getItemId())
                    .orElseThrow(() -> new ItemException(ExceptionCode.NOT_FOUND_ITEM));

        ItemUpdateStatus itemUpdateStatus = null;
        if(updateStockByRedis(item.getId(), request.getQuantity())) itemUpdateStatus = (request.getQuantity() >= 0) ? ItemUpdateStatus.SUCCEED : ItemUpdateStatus.CANCELED;
        else itemUpdateStatus = ItemUpdateStatus.OUT_OF_STOCK;

        ItemUpdateLog itemUpdateLog = ItemUpdateLog.from(itemUpdateStatus, orderId, customerAccountId, request);
        itemUpdateLogRepository.save(itemUpdateLog);
        return ItemStockUpdateDto.fromItemUpdateLog(itemUpdateLog);
    }

    private boolean updateStockByRedis(Long itemId, Long quantity) {
        Long stock = itemRedisRepository.decrementItemStock(itemId, quantity);
        if(stock >= 0) return true;

        itemRedisRepository.incrementItemStock(itemId, quantity);
        return false;
    }

    @Transactional
    public ItemStockUpdateDto decreaseStockByPessimisticLock(Long orderId, Long customerAccountId, ItemStockUpdateDto request) {
        try {
            Item item = itemRepository.findByIdWithPessimisticLock(request.getItemId())
                    .orElseThrow(() -> new ItemException(ExceptionCode.NOT_FOUND_ITEM));
            item.decreaseStock(request.getQuantity());

            ItemUpdateLog itemUpdateLog = ItemUpdateLog.from(ItemUpdateStatus.SUCCEED, orderId, customerAccountId, request);
            itemUpdateLogRepository.save(itemUpdateLog);
            return ItemStockUpdateDto.fromItemUpdateLog(itemUpdateLog);
        } catch (ItemException | StockException e) {
            ItemUpdateLog itemUpdateLog = ItemUpdateLog.from(ItemUpdateStatus.FAILED, orderId, customerAccountId, request);
            itemUpdateLogRepository.save(itemUpdateLog);
            return ItemStockUpdateDto.fromItemUpdateLog(itemUpdateLog);
        }
    }
}