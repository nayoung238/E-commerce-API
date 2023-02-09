package com.nayoung.itemservice.web;

import com.nayoung.itemservice.domain.ItemService;
import com.nayoung.itemservice.web.dto.ItemCreationRequest;
import com.nayoung.itemservice.web.dto.ItemResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("item-service")
public class ItemController {

    private final ItemService itemService;

    @GetMapping("/items/{itemId}")
    public ResponseEntity<?> getItem(@PathVariable Long itemId) {
        ItemResponse response = itemService.getItemById(itemId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/items/create")
    public ResponseEntity<?> create(@RequestBody ItemCreationRequest request) {
        ItemResponse response = itemService.createItem(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
