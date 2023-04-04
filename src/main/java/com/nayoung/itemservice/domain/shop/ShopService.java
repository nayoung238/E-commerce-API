package com.nayoung.itemservice.domain.shop;

import com.nayoung.itemservice.web.dto.ShopCreationRequest;

import java.util.List;

public interface ShopService {

    void create(ShopCreationRequest request);
    Shop findShopById(Long shopId);
    List<Shop> findShops(String province, String city);
}
