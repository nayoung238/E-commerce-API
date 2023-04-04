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
    public ItemResponse getItemById(ItemInfoRequest request) {
        Item item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new ItemException(ExceptionCode.NOT_FOUND_ITEM));

        DiscountCode discountCode = DiscountCode.getDiscountCode(request.getCustomerRating());
        return ItemResponse.fromItemEntityAndApplyDiscount(item, discountCode.percentage);
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
