package com.nayoung.itemservice.web;

import com.nayoung.itemservice.domain.item.ItemService;
import com.nayoung.itemservice.web.dto.ItemCreationRequest;
import com.nayoung.itemservice.web.dto.ItemInfoByItemIdRequest;
import com.nayoung.itemservice.web.dto.ItemInfoByShopLocationRequest;
import com.nayoung.itemservice.web.dto.ItemResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/")
public class ItemController {

    private final ItemService itemService;

    @GetMapping("/item")
    public ResponseEntity<?> findItemByItemId(@RequestBody ItemInfoByItemIdRequest request) {
        ItemResponse response = itemService.findItemByItemId(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/items/")
    public ResponseEntity<?> findItemByShopLocation(@RequestBody ItemInfoByShopLocationRequest request) {
        List<ItemResponse> response = itemService.findItemsByItemName(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/items/create")
    public ResponseEntity<?> create(@RequestBody ItemCreationRequest request) {
        ItemResponse response = itemService.createItem(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
