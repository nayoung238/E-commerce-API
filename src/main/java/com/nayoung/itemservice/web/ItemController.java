package com.nayoung.itemservice.web;

import com.nayoung.itemservice.domain.item.ItemService;
import com.nayoung.itemservice.web.dto.ItemCreationRequest;
import com.nayoung.itemservice.web.dto.ItemInfoRequest;
import com.nayoung.itemservice.web.dto.ItemResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/")
public class ItemController {

    private final ItemService itemService;

    @GetMapping("/items/")
    public ResponseEntity<?> getItem(@RequestBody ItemInfoRequest request) {
        ItemResponse response = itemService.getItemById(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/items/create")
    public ResponseEntity<?> create(@RequestBody ItemCreationRequest request) {
        ItemResponse response = itemService.createItem(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
