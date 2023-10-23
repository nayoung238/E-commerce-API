package com.nayoung.itemservice.web;

import com.nayoung.itemservice.domain.item.ItemService;
import com.nayoung.itemservice.exception.ItemException;
import com.nayoung.itemservice.web.dto.*;
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
        ItemDto response = itemService.findItemByItemId(request.getItemId(), request.getCustomerRating());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/items/")
    public ResponseEntity<?> findItemByShopLocation(@RequestBody ItemListRequestDto request) {
        List<ItemDto> response = itemService.findItems(request.getItemName(), request.getLocation(), request.getCustomerRating());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/items/create")
    public ResponseEntity<?> create(@RequestBody ItemDto request) {
        ItemDto response = itemService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/item-update-log/{eventId}")
    public ResponseEntity<?> findAllItemUpdateLogByEventId(@PathVariable String eventId) {
        try {
            List<ItemUpdateLogDto> itemUpdateLogDtoList = itemService.findAllItemUpdateLogByEventId(eventId);
            return ResponseEntity.status(HttpStatus.OK).body(itemUpdateLogDtoList);
        } catch (ItemException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e);
        }
    }
}