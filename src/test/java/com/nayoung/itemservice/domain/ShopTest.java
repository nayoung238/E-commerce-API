package com.nayoung.itemservice.domain;

//import com.nayoung.itemservice.domain.shop.Shop;
//import com.nayoung.itemservice.domain.shop.ShopRepository;
//import com.nayoung.itemservice.domain.shop.ShopService;
//import com.nayoung.itemservice.domain.shop.location.LocationCode;
//import com.nayoung.itemservice.exception.LocationException;
//import com.nayoung.itemservice.exception.ShopException;
//import com.nayoung.itemservice.web.dto.ShopDto;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import java.util.List;
//
//@SpringBootTest
//public class ShopTest {
//
//    @Autowired
//    private ShopService shopService;
//    @Autowired
//    private ShopRepository shopRepository;
//
//    private final String SEOUL = "seoul";
//    private final String SUWON = "suwon";
//    private final String USA = "USA";
//
//    @AfterEach
//    public void afterEach() {
//        shopRepository.deleteAll();
//    }
//
//    @Test
//    public void 상점_생성 () {
//        // 지원하는 지역
//        ShopDto request1 = ShopDto.builder().location(SEOUL).name("songpa-1").build();
//        shopService.create(request1);
//        List<Shop> shops = shopRepository.findAllByLocationCode(LocationCode.SEOUL);
//        Assertions.assertEquals(1L, shops.size());
//
//        // 지원하지 않는 지역
//        ShopDto request2 = ShopDto.builder().location(USA).name("songpa-1").build();
//        Assertions.assertThrows(LocationException.class, () -> shopService.create(request2));
//    }
//
//    @Test
//    public void 지역기준으로_상점_찾기 () {
//        List<Shop> shops = shopService.findAllShopByLocation(SEOUL);
//        List<Shop> shopsByRepo = shopRepository.findAllByLocationCode(LocationCode.SEOUL);
//        Assertions.assertEquals(shopsByRepo.size(), shops.size());
//
//        shops = shopService.findAllShopByLocation(SUWON);
//        shopsByRepo = shopRepository.findAllByLocationCode(LocationCode.SUWON);
//        Assertions.assertEquals(shopsByRepo.size(), shops.size());
//    }
//
//    @Test
//    void 상점이름_중복검사 () {
//        ShopDto request = ShopDto.builder()
//                .location(SEOUL).name("songpa-1").build();
//
//        shopService.create(request);
//        Assertions.assertThrows(ShopException.class, () -> shopService.create(request));
//        Assertions.assertEquals(1L, shopRepository.countByName("songpa-1"));
//    }
//}
