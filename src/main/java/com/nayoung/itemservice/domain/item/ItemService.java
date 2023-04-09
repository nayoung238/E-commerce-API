package com.nayoung.itemservice.domain.item;

import com.nayoung.itemservice.web.dto.*;

import java.util.List;

public interface ItemService {

    ItemResponse createItem(ItemCreationRequest itemCreationRequest);
    ItemResponse findItemByItemId(ItemInfoByItemIdRequest request);
    List<ItemResponse> findItemsByItemName(ItemInfoByShopLocationRequest request);
    ItemResponse update(ItemInfoUpdateRequest itemInfoUpdateRequest);
    ItemStockUpdateResponse updateItemsStock(ItemStockUpdateRequest request);
}
