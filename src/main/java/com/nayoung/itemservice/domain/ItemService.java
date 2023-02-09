package com.nayoung.itemservice.domain;

import com.nayoung.itemservice.web.dto.ItemCreationRequest;
import com.nayoung.itemservice.web.dto.ItemResponse;

public interface ItemService {

    ItemResponse createItem(ItemCreationRequest itemCreationRequest);
    ItemResponse getItemById(Long id);
}
