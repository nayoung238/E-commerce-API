package com.nayoung.itemservice.domain.item;

import com.nayoung.itemservice.domain.discount.DiscountCode;
import com.nayoung.itemservice.domain.shop.Shop;
import com.nayoung.itemservice.domain.shop.ShopService;
import com.nayoung.itemservice.exception.ExceptionCode;
import com.nayoung.itemservice.exception.ItemException;
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

    @Override
    public ItemResponse createItem(ItemCreationRequest request) {
        Shop shop = shopService.findShopById(request.getShopId());
        Item item = Item.fromItemCreationRequestAndShopEntity(request, shop);
        Item savedItem = itemRepository.save(item);
        return ItemResponse.fromItemEntity(savedItem);
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
    public void decreaseStock(Long id, Long quantity) {
        Item item = itemRepository.findByIdWithPessimisticLock(id).orElseThrow();
        item.decreaseStock(quantity);
    }
}
