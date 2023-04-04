package com.nayoung.itemservice.domain;

import com.nayoung.itemservice.domain.discount.DiscountCode;
import com.nayoung.itemservice.domain.discount.DiscountService;
import com.nayoung.itemservice.domain.item.Item;
import com.nayoung.itemservice.domain.item.ItemRepository;
import com.nayoung.itemservice.domain.item.ItemService;
import com.nayoung.itemservice.domain.shop.Shop;
import com.nayoung.itemservice.domain.shop.ShopRepository;
import com.nayoung.itemservice.domain.shop.ShopService;
import com.nayoung.itemservice.exception.DiscountException;
import com.nayoung.itemservice.exception.ItemException;
import com.nayoung.itemservice.web.dto.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

@SpringBootTest
public class DiscountTest {

    @Autowired
    private ItemService itemService;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private ShopService shopService;
    @Autowired
    private ShopRepository shopRepository;
    @Autowired
    private DiscountService discountService;

    private final String itemName = "apple";
    private final Long itemPrice = 2000L;
    private final Integer testItemDiscountPercentage = 7;

    private final String SEOUL = "seoul";
    private final String KYEONGGI = "kyeonggi";
    private final String SUWON = "suwon";

    @BeforeEach
    public void beforeEach() {
        createItem();
    }

    @AfterEach
    public void afterEach() {
        itemRepository.deleteAll();
        shopRepository.deleteAll();
    }

    @Test
    @DisplayName("회원 등급에 따른 할인 테스트")
    public void discountTest() {
        Optional<Item> item = itemRepository.findById(2L);
        assert(item.isPresent());

        /*
         test에서 사용하는 모든 item의 discountPercentage는 7(testItemDiscountPercentage)로 설정
         customerRating이 GOLD일 경우 discountPercentage가 더 높은 GOLD.percentage(10) 가 적용됨
         */
        ItemInfoByItemIdRequest request1 = ItemInfoByItemIdRequest.builder()
                .itemId(2L).customerRating("GOLD").build();
        ItemResponse response = itemService.findItemByItemId(request1);

        long expectedPrice =  item.get().getPrice() * (100 - DiscountCode.GOLD.percentage) / 100;
        Assertions.assertEquals(expectedPrice, response.getDiscountedPrice());

        /*
         test에서 사용하는 모든 item의 discountPercentage는 7(testItemDiscountPercentage)로 설정
         customerRating이 UNQUALIFIED일 경우 discountPercentage가 더 높은 testItemDiscountPercentage(7)가 적용됨
         */
        ItemInfoByItemIdRequest request2 = ItemInfoByItemIdRequest.builder()
                .itemId(2L).customerRating("UNQUALIFIED").build();
        response = itemService.findItemByItemId(request2);

        expectedPrice = item.get().getPrice() * (100 - item.get().getDiscountPercentage()) / 100;
        Assertions.assertEquals(expectedPrice, response.getDiscountedPrice());
    }

    @Test
    @DisplayName("매칭되는 할인코드 없음")
    public void DiscountExceptionTest() {
        ItemInfoByShopLocationRequest request = ItemInfoByShopLocationRequest.builder()
                .itemName(itemName).customerRating("DIAMOND")
                .province("none").city("none").build();

        Assertions.assertThrows(DiscountException.class,
                () -> itemService.findItemsByItemName(request));
    }

    private void createItem() {
        createShops();

        List<Shop> seoulShops = shopService.findShops(SEOUL, SEOUL);
        List<Shop> suwonShops = shopService.findShops(KYEONGGI, SUWON);
        assert(seoulShops.size() > 0);
        assert(suwonShops.size() > 0);

        ItemCreationRequest request = ItemCreationRequest.builder()
                .name(itemName).price(itemPrice).stock(100L)
                .build();

        for(int i = 0; i < 2; i++) {
            Long randomId = seoulShops.get((int)(Math.random() * (seoulShops.size()))).getId();
            request.setShopId(randomId);
            try {
                ItemResponse response = itemService.createItem(request);
                DiscountCreationRequest discountCreationRequest = DiscountCreationRequest.builder()
                        .itemId(response.getItemId()).discountPercentage(testItemDiscountPercentage).build();
                discountService.applyDiscount(discountCreationRequest);
            } catch(ItemException ignored) {}
        }

        for(int i = 0; i < 3; i++) {
            Long randomId = suwonShops.get((int)(Math.random() * (suwonShops.size()))).getId();
            request.setShopId(randomId);
            try {
                ItemResponse response = itemService.createItem(request);
                DiscountCreationRequest discountCreationRequest = DiscountCreationRequest.builder()
                        .itemId(response.getItemId()).discountPercentage(testItemDiscountPercentage).build();
                discountService.applyDiscount(discountCreationRequest);
            } catch(ItemException ignored) {}
        }
    }

    private void createShops() {
        for(int i = 0; i < 3; i++) {
            ShopCreationRequest request = ShopCreationRequest.builder()
                    .province(SEOUL).city(SEOUL)
                    .name(SEOUL + i).build();
            shopService.create(request);
        }

        for(int i = 0; i < 4; i++) {
            ShopCreationRequest request = ShopCreationRequest.builder()
                    .province(KYEONGGI).city(SUWON)
                    .name(SUWON + i).build();
            shopService.create(request);
        }
    }
}
