package com.nayoung.itemservice.domain;

import com.nayoung.itemservice.web.dto.ItemCreationRequest;
import com.nayoung.itemservice.web.dto.ItemResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;

    @Override
    public ItemResponse createItem(ItemCreationRequest itemCreationRequest) {
        Item item = Item.fromItemCreationRequest(itemCreationRequest);
        Item savedItem = itemRepository.save(item);
        return ItemResponse.fromItemEntity(savedItem);
    }

    @Override
    public ItemResponse getItemById(Long id) {
        Item item = itemRepository.findById(id).orElseThrow();
        return ItemResponse.fromItemEntity(item);
    }
}
