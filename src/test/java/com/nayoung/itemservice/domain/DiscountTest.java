package com.nayoung.itemservice.domain;

import com.nayoung.itemservice.domain.discount.DiscountCode;
import com.nayoung.itemservice.domain.item.Item;
import com.nayoung.itemservice.domain.item.ItemRepository;
import com.nayoung.itemservice.domain.item.ItemService;
import com.nayoung.itemservice.exception.DiscountException;
import com.nayoung.itemservice.exception.ExceptionCode;
import com.nayoung.itemservice.exception.ItemException;
import com.nayoung.itemservice.web.dto.ItemCreationRequest;
import com.nayoung.itemservice.web.dto.ItemInfoRequest;
import com.nayoung.itemservice.web.dto.ItemResponse;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class DiscountTest {

    @Autowired
    private ItemService itemService;
    @Autowired
    private ItemRepository itemRepository;

    @BeforeEach
    public void beforeEach() {
        ItemCreationRequest request = ItemCreationRequest.builder()
                .name("apple").price(2000L).stock(100L)
                .build();
        itemService.createItem(request);
    }

    @AfterEach
    public void afterEach() {
        itemRepository.deleteAll();
    }

    @Test
    @DisplayName("회원 등급에 따른 할인 테스트")
    public void discountTest() {
        ItemInfoRequest request1 = ItemInfoRequest.builder()
                .itemId(1L).customerRating("GOLD").build();

        ItemInfoRequest request2 = ItemInfoRequest.builder()
                .itemId(1L).customerRating("UNQUALIFIED").build();

        Item item = itemRepository.findById(request1.getItemId())
                .orElseThrow(() -> new ItemException(ExceptionCode.NOT_FOUND_ITEM));

        ItemResponse response = itemService.getItemById(request1);
        long expectedPrice = item.getPrice() * (100 - DiscountCode.GOLD.percentage) / 100;
        Assertions.assertEquals(expectedPrice, response.getDiscountedPrice());

        response = itemService.getItemById(request2);
        expectedPrice = item.getPrice() * (100 - DiscountCode.NONE.percentage) / 100;
        Assertions.assertEquals(expectedPrice, response.getDiscountedPrice());
        Assertions.assertEquals(item.getPrice(), response.getDiscountedPrice());
    }

    @Test
    public void DiscountExceptionTest() {
        ItemInfoRequest request = ItemInfoRequest.builder()
                .itemId(1L).customerRating("DIAMOND").build();

        Item item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new ItemException(ExceptionCode.NOT_FOUND_ITEM));

        Assertions.assertThrows(DiscountException.class, () -> itemService.getItemById(request));
    }
}
