package com.ecommerce.itemservice.web;

import com.ecommerce.itemservice.web.dto.ShopDto;
import com.ecommerce.itemservice.domain.shop.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/")
public class ShopController {

    private final ShopService shopService;

    @PostMapping("/shops/create")
    public ResponseEntity<?> create(@RequestBody ShopDto request) {
        ShopDto response = shopService.create(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
