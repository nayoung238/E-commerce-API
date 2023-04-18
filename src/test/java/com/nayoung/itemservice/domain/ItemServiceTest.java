package com.nayoung.itemservice.domain;

import com.nayoung.itemservice.domain.discount.DiscountCode;
import com.nayoung.itemservice.domain.discount.DiscountService;
import com.nayoung.itemservice.domain.item.Item;
import com.nayoung.itemservice.domain.item.ItemRepository;
import com.nayoung.itemservice.domain.item.ItemService;
import com.nayoung.itemservice.domain.item.log.OrderStatus;
import com.nayoung.itemservice.domain.shop.Shop;
import com.nayoung.itemservice.domain.shop.ShopRepository;
import com.nayoung.itemservice.domain.shop.ShopService;
import com.nayoung.itemservice.exception.ItemException;
import com.nayoung.itemservice.web.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@SpringBootTest
public class ItemServiceTest {

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

    private static final int threadCount = 5;

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
    @DisplayName("원하는 지역에서 할인 적용한 아이템 가져오기")
    public void getItemsByShopLocationAndApplyDiscountTest() {
        /*
         test에서 사용하는 모든 item의 discountPercentage는 7(testItemDiscountPercentage)로 설정
         customerRating이 GOLD일 경우 discountPercentage가 더 높은 GOLD.percentage(10) 가 적용됨
         */
        ItemInfoByShopLocationRequest request = ItemInfoByShopLocationRequest.builder()
                .itemName(itemName).customerRating("GOLD")
                .province(KYEONGGI).city(SUWON).build();

        List<ItemResponse> responses = itemService.findItemsByItemName(request);
        assert(responses.size() > 0);
        Long exceptedPriceByGold = itemPrice * (100 - DiscountCode.GOLD.percentage) / 100;
        Assertions.assertTrue(responses.stream()
                .anyMatch(itemResponse -> Objects.equals(itemResponse.getDiscountedPrice(), exceptedPriceByGold)));

        /*
         test에서 사용하는 모든 item의 discountPercentage는 7(testItemDiscountPercentage)로 설정
         customerRating이 SILVER일 경우 discountPercentage가 더 높은 testItemDiscountPercentage(7)가 적용됨
         */
        request = ItemInfoByShopLocationRequest.builder()
                .itemName(itemName).customerRating("SILVER")
                .province(SEOUL).city(SEOUL).build();

        responses = itemService.findItemsByItemName(request);
        assert(responses.size() > 0);
        Long exceptedPriceBySilver = itemPrice * (100 - testItemDiscountPercentage) / 100;
        Assertions.assertTrue(responses.stream()
                .anyMatch(itemResponse -> Objects.equals(itemResponse.getDiscountedPrice(), exceptedPriceBySilver)));
    }

    @Test
    @DisplayName("Lost Update 발생하지 않는지 테스트")
    public void stockUpdateTest() throws InterruptedException {
        // 같은 상품 업데이트
        List<ItemInfoUpdateRequest> requestList = getItemUpdateInfoList(5);
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        CountDownLatch countDownLatch = new CountDownLatch(requestList.size());

        assert (requestList.size() > 0);
        Item item = itemRepository.findById(requestList.get(0).getItemId()).orElseThrow();
        for(ItemInfoUpdateRequest request : requestList) {
            executorService.submit(() -> {
                try {
                    itemService.update(request);
                } catch (Exception e) {
                    log.error(e.getMessage());
                } finally {
                    countDownLatch.countDown();
                }
            });
        }
        countDownLatch.await();

        Long totalQuantity = requestList.parallelStream()
                .map(ItemInfoUpdateRequest::getAdditionalQuantity)
                .reduce(0L, Long::sum);

        Item updatedItem = itemRepository.findById(1L).orElseThrow();
        Assertions.assertEquals(item.getStock() + totalQuantity , updatedItem.getStock());
    }

     @Test
     @DisplayName("StockException 처리 확인 테스트")
     public void stockExceptionTest() throws InterruptedException {
        Item item = itemRepository.findById(1L).orElseThrow();
        Long count = item.getStock();

        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CountDownLatch countDownLatch = new CountDownLatch(Math.toIntExact(count + 2));

        OrderItemRequest orderItemRequest = OrderItemRequest.builder()
                .shopId(item.getShop().getId()).itemId(item.getId())
                .quantity(1L).build();

        List<OrderItemResponse> responses = new ArrayList<>();
        for (long i = 0; i < count + 2; i++) {
            executorService.submit(() -> {
                try {
                    responses.add(itemService.decreaseStockByPessimisticLock(1L, orderItemRequest));
                } catch(Exception e) {
                    log.error(e.getMessage());
                } finally {
                    countDownLatch.countDown();
                }
            });
        }
        countDownLatch.await();

        Assertions.assertTrue(responses.parallelStream()
                .anyMatch(response -> Objects.equals(OrderStatus.FAILED, response.getOrderStatus())));
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

    private List<ItemInfoUpdateRequest> getItemUpdateInfoList(int n) {
        List<ItemInfoUpdateRequest> itemInfoUpdateRequestList = new ArrayList<>();
        for(int i = 0; i < n; i++)
            itemInfoUpdateRequestList.add(getItemInfoUpdateRequest());

        return itemInfoUpdateRequestList;
    }

    private ItemInfoUpdateRequest getItemInfoUpdateRequest() {
        ItemInfoUpdateRequest request = new ItemInfoUpdateRequest();
        request.setItemId(1L);
        request.setName("apple");
        request.setPrice(1300L);
        request.setAdditionalQuantity(10L);
        return request;
    }
}
