package com.nayoung.itemservice.domain;

import com.nayoung.itemservice.domain.shop.Shop;
import com.nayoung.itemservice.domain.shop.ShopRepository;
import com.nayoung.itemservice.domain.shop.ShopService;
import com.nayoung.itemservice.domain.shop.location.CityCode;
import com.nayoung.itemservice.domain.shop.location.ProvinceCode;
import com.nayoung.itemservice.exception.LocationException;
import com.nayoung.itemservice.web.dto.ShopCreationRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class ShopTest {

    @Autowired
    private ShopService shopService;
    @Autowired
    private ShopRepository shopRepository;

    private final String SEOUL = "seoul";
    private final String KYEONGGI = "kyeonggi";
    private final String SUWON = "suwon";
    private final String NONE = "none";
    private final String USA = "USA";

    @AfterEach
    public void afterEach() {
        shopRepository.deleteAll();
    }

    @Test
    void createShopTest() {
        // SEOUL
        ShopCreationRequest request = ShopCreationRequest.builder().province(SEOUL).city(SEOUL).build();
        shopService.create(request);
        List<Shop> shops = shopRepository.findAllByLocationProvinceAndLocationCity(ProvinceCode.SEOUL, CityCode.SEOUL);
        Assertions.assertTrue(shops.size() > 0);

        // KYEONGGI - SUWON
        request = ShopCreationRequest.builder().province(KYEONGGI).city(SUWON).build();
        for(int i = 0; i < 4; i++) shopService.create(request);
        shops = shopRepository.findAllByLocationProvinceAndLocationCity(ProvinceCode.KYEONGGI, CityCode.SUWON);
        Assertions.assertEquals(4, shops.size());

        // USA
        request = ShopCreationRequest.builder().province(USA).city(USA).build();
        ShopCreationRequest finalRequest = request;
        Assertions.assertThrows(LocationException.class, () -> shopService.create(finalRequest));
    }

    @Test
    void findShopsTest() {
        List<Shop> shops = shopService.findShops(SEOUL, SEOUL);
        List<Shop> shopsByRepo = shopRepository.findAllByLocationProvinceAndLocationCity(ProvinceCode.SEOUL, CityCode.SEOUL);
        Assertions.assertEquals(shopsByRepo.size(), shops.size());

        shops = shopService.findShops(KYEONGGI, SUWON);
        shopsByRepo = shopRepository.findAllByLocationProvinceAndLocationCity(ProvinceCode.KYEONGGI, CityCode.SUWON);
        Assertions.assertEquals(shopsByRepo.size(), shops.size());

        shops = shopService.findShops(NONE, NONE);
        Assertions.assertEquals(shopRepository.findAll().size(), shops.size());
    }
}
