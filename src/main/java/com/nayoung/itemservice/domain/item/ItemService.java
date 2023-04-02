package com.nayoung.itemservice.domain.item;

import com.nayoung.itemservice.web.dto.ItemCreationRequest;
import com.nayoung.itemservice.web.dto.ItemInfoRequest;
import com.nayoung.itemservice.web.dto.ItemResponse;
import com.nayoung.itemservice.web.dto.ItemInfoUpdateRequest;

public interface ItemService {

    ItemResponse createItem(ItemCreationRequest itemCreationRequest);
    ItemResponse getItemById(ItemInfoRequest request);
    ItemResponse update(ItemInfoUpdateRequest itemInfoUpdateRequest);
    void decreaseStock(Long id, Long quantity);
}
