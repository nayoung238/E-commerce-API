package com.nayoung.itemservice.domain;

import com.nayoung.itemservice.domain.item.Item;
import com.nayoung.itemservice.domain.item.ItemRepository;
import com.nayoung.itemservice.domain.item.ItemService;
import com.nayoung.itemservice.domain.item.log.OrderStatus;
import com.nayoung.itemservice.domain.shop.Shop;
import com.nayoung.itemservice.domain.shop.ShopRepository;
import com.nayoung.itemservice.domain.shop.ShopService;
import com.nayoung.itemservice.domain.shop.location.LocationCode;
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
import java.util.stream.Collectors;

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

    List<String> location = List.of(new String[]{"seoul", "suwon"});
    List<String> itemName = List.of(new String[]{"apple", "banana", "kiwi", "egg"});
    private final Long PRICE = 1000L;
    private final Long INITIAL_QUANTITY = 10L;

    private final String SEOUL = "seoul";
    private final String NONE = "none";

    @BeforeEach
    public void beforeEach() {
        createShops(8);
        List<Shop> shops = shopRepository.findAll();

        // 서울 지역에서는 egg 상품을 팔지 않음
        for(Shop shop : shops) {
            int size = Objects.equals(shop.getLocationCode(), LocationCode.SEOUL) ? 3 : 4;
            for(int i = 0; i < size; i++) {
                ItemDto itemCreationDto = ItemDto.builder()
                        .shopId(shop.getId())
                        .name(itemName.get(i))
                        .price(PRICE)
                        .discountPercentage(0)
                        .stock(INITIAL_QUANTITY).build();

                itemService.create(itemCreationDto);
            }
        }
    }

    @AfterEach
    public void afterEach() {
        itemRepository.deleteAll();
        shopRepository.deleteAll();
    }

    @Test
    @DisplayName("원하는 지역이 없는 경우 모든 지역을 기준으로 탐색")
    public void 원하는_지역_없는_경우() {
        List<ItemDto> itemDtos = itemService.findItems(itemName.get(0), NONE, "UNQUALIFIED");

        List<Item> items = itemRepository.findAllByName(itemName.get(0));
        Assertions.assertEquals(items.size(), itemDtos.size());
    }

    @Test
    public void 원하는_지역에_상품있는_경우() {
        List<ItemDto> itemDtos = itemService.findItems(itemName.get(0), SEOUL, "WELCOME");

        List<Item> items = itemRepository.findAllByName(itemName.get(0));
        items = items.stream()
                .filter(item -> Objects.equals(item.getShop().getLocationCode(), LocationCode.SEOUL))
                .collect(Collectors.toList());
        Assertions.assertEquals(items.size(), itemDtos.size());
    }

    @Test
    @DisplayName("모든 지역을 기준으로 탐색")
    public void 원하는_지역있지만_상품없는_경우() {
        // 서울 지역에서는 egg 상품(index 3)을 팔지 않음
        List<ItemDto> itemDtos = itemService.findItems(itemName.get(3), SEOUL, "WELCOME");

        List<Item> items = itemRepository.findAllByName(itemName.get(3));
        Assertions.assertTrue(
                items.stream().noneMatch(i -> Objects.equals(i.getShop().getLocationCode(), LocationCode.SEOUL))
        );
        Assertions.assertEquals(items.size(), itemDtos.size());
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

        ItemStockToUpdateDto orderItemRequest = ItemStockToUpdateDto.builder()
                .shopId(item.getShop().getId()).itemId(item.getId())
                .quantity(1L).build();

        List<ItemStockToUpdateDto> responses = new ArrayList<>();
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

    private void createShops(int numberOfShops) {
        for(int i = 0; i < numberOfShops; i++) {
            ShopDto shopDTO = ShopDto.builder()
                    .location(location.get(i % 2))
                    .name("shop-" +  location.get(i % 2) + "-" + i)
                    .build();

            shopService.create(shopDTO);
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
