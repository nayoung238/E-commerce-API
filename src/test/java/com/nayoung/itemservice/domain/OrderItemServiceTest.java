package com.nayoung.itemservice.domain;

import com.nayoung.itemservice.domain.item.Item;
import com.nayoung.itemservice.domain.item.ItemRepository;
import com.nayoung.itemservice.domain.item.ItemService;
import com.nayoung.itemservice.domain.item.OrderItemService;
import com.nayoung.itemservice.domain.item.log.ItemUpdateLog;
import com.nayoung.itemservice.domain.item.log.ItemUpdateLogRepository;
import com.nayoung.itemservice.domain.item.log.OrderStatus;
import com.nayoung.itemservice.domain.shop.Shop;
import com.nayoung.itemservice.domain.shop.ShopRepository;
import com.nayoung.itemservice.domain.shop.ShopService;
import com.nayoung.itemservice.exception.ExceptionCode;
import com.nayoung.itemservice.exception.ItemException;
import com.nayoung.itemservice.web.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Slf4j
@SpringBootTest
public class OrderItemServiceTest {

    @Autowired private ItemService itemService;
    @Autowired private ItemRepository itemRepository;
    @Autowired private OrderItemService orderItemService;
    @Autowired private ItemUpdateLogRepository itemUpdateLogRepository;
    @Autowired private ShopService shopService;
    @Autowired private ShopRepository shopRepository;

    private final Long ORDER_ID = 3L;
    private final Long CUSTOMER_ACCOUNT_ID = 2L;
    private final String SUWON = "suwon";

    @BeforeEach
    public void beforeEach() {
        createShops();
        createItems();
    }

    @AfterEach
    public void afterEach() {
        itemRepository.deleteAll();
        itemUpdateLogRepository.deleteAll();
        shopRepository.deleteAll();
    }

    @Test
    @DisplayName("정상 주문에 따른 재고 수정 테스트")
    public void updateItemStockTest() {
        List<OrderItemRequest> orderItemRequests = getOrderItemRequests();
        Map<Long, Long> previousStockMap = getPreviousStock(orderItemRequests);

        ItemStockUpdateRequest request = ItemStockUpdateRequest.forTest(ORDER_ID, CUSTOMER_ACCOUNT_ID, orderItemRequests);
        ItemStockUpdateResponse response = orderItemService.updateItemsStock(request);

        Assertions.assertTrue(response.getIsAvailableToOrder());
        List<ItemUpdateLog> itemUpdateLogs = itemUpdateLogRepository.findAllByOrderId(response.getOrderId());
        Assertions.assertTrue(itemUpdateLogs.stream()
                .allMatch(itemUpdateLog -> itemUpdateLog.getOrderStatus() == OrderStatus.SUCCEED));

        for(ItemUpdateLog itemUpdateLog : itemUpdateLogs) {
            Long previousStock = previousStockMap.get(itemUpdateLog.getItemId());
            Item item = itemRepository.findById(itemUpdateLog.getItemId()).orElseThrow();
            Assertions.assertEquals(previousStock, item.getStock() + itemUpdateLog.getQuantity());
        }
    }

    @Test
    @DisplayName("여러 사용자가 같은 상품 리스트를 주문하는 테스트")
    void multipleOrdersTest() throws InterruptedException {
        List<ItemStockUpdateRequest> requests = getItemStockUpdateRequests();
        assert(requests.size() > 0);
        Map<Long, Long> previousStock = getPreviousStock(requests.get(0).getOrderItemRequests());

        ExecutorService executorService = Executors.newFixedThreadPool(5);
        CountDownLatch countDownLatch = new CountDownLatch(requests.size());

        List<ItemStockUpdateResponse> responses = new ArrayList<>();
        for(ItemStockUpdateRequest request : requests) {
            executorService.submit(() -> {
                try {
                    responses.add(orderItemService.updateItemsStock(request));
                } catch (Exception e) {
                    log.error(e.getMessage());
                } finally {
                    countDownLatch.countDown();
                }
            });
        }
        countDownLatch.await();

        // 모든 주문 아이템에 대한 응답을 받았는지
        Assertions.assertTrue(responses.stream()
                .allMatch(r -> r.getOrderItemResponses().size() == requests.get(0).getOrderItemRequests().size()));

        // 현재 재고 + 주문된 수량 == 이전 재고
        Long itemId = requests.get(0).getOrderItemRequests().get(0).getItemId();
        List<ItemUpdateLog> logs = itemUpdateLogRepository.findAllByItemIdAndOrderStatus(itemId, OrderStatus.SUCCEED);
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemException(ExceptionCode.NOT_FOUND_ITEM));

        Long expectedStock = previousStock.get(itemId);
        Long calcStock = logs.stream().map(ItemUpdateLog::getQuantity).reduce(item.getStock(), Long::sum);
        Assertions.assertEquals(expectedStock, calcStock);
    }

    @Test
    @DisplayName("1개의 상품이라도 재고가 부족하면 모든 주문 상품 취소 처리")
    public void undoTest() {
        // 첫 번째 아이템은 주문 가능, 두 번째 아이템은 재고 부족
        List<OrderItemRequest> orderItemRequests = getOrderItemRequestsByExcessQuantity();
        ItemStockUpdateRequest request = ItemStockUpdateRequest.forTest(ORDER_ID, CUSTOMER_ACCOUNT_ID, orderItemRequests);
        Map<Long, Long> previousStockMap = getPreviousStock(orderItemRequests);

        ItemStockUpdateResponse response = orderItemService.updateItemsStock(request);
        // 주문에 대한 실패 상태
        Assertions.assertFalse(response.getIsAvailableToOrder());
        // 각 아이템에 대한 실패 상태
        Assertions.assertTrue(response.getOrderItemResponses().stream()
                .allMatch(orderItemResponse -> Objects.equals(orderItemResponse.getOrderStatus(), OrderStatus.FAILED)));

        // UNDO check
        // OrderStatus 상태가 FAILED(재고 부족) 또는 CANCELED(다른 아이템에 의해 취소됨)
        List<ItemUpdateLog> logs = itemUpdateLogRepository.findAllByOrderId(response.getOrderId());
        Assertions.assertTrue(logs.stream().noneMatch(l -> l.getOrderStatus() == OrderStatus.SUCCEED));
    }

    private Map<Long, Long> getPreviousStock(List<OrderItemRequest> requests) {
        Map<Long, Long> previousStock = new HashMap<>();
        for(OrderItemRequest request : requests) {
            Item item = itemRepository.findById(request.getItemId())
                    .orElseThrow(() -> new ItemException(ExceptionCode.NOT_FOUND_ITEM));
            previousStock.put(item.getId(), item.getStock());
        }
        return previousStock;
    }

    private List<ItemStockUpdateRequest> getItemStockUpdateRequests() {
        List<OrderItemRequest> orderItemRequests = getOrderItemRequests();
        List<ItemStockUpdateRequest> result = new ArrayList<>();
        for(int i = 1; i <= 4; i++) {
            result.add(ItemStockUpdateRequest.forTest((long)i + 2, (long)i, orderItemRequests));
        }
        return result;
    }


    private List<OrderItemRequest> getOrderItemRequests() {
        List<Item> items = itemRepository.findAll();
        assert(items.size() > 0);

        return items.stream()
                .map(i -> OrderItemRequest.forTest(i.getShop().getId(), i.getId(), 4L))
                .collect(Collectors.toList());
    }

    private List<OrderItemRequest> getOrderItemRequestsByExcessQuantity() {
        List<Item> items = itemRepository.findAll();
        assert(items.size() == 2);

        List<OrderItemRequest> orderItemRequests = new ArrayList<>();
        orderItemRequests.add(OrderItemRequest.forTest(
                items.get(0).getShop().getId(), items.get(0).getId(), items.get(0).getStock() / 2));
        orderItemRequests.add(OrderItemRequest.forTest(
                items.get(1).getShop().getId(), items.get(1).getId(), items.get(1).getStock() + 1)); // 재고보다ㅒ많은 주문

        return orderItemRequests;
    }

    private void createShops() {
        ShopDto request = ShopDto.builder()
                .city(SUWON)
                .name(SUWON + 1).build();

        shopService.create(request);
    }

    private void createItems() {
        List<Shop> suwonShops = shopService.findAllShopByCity(SUWON);
        assert(suwonShops.size() == 1);

        ItemCreationRequest request1 = ItemCreationRequest.builder()
                .name("apple").price(1200L).stock(20L)
                .build();

        request1.setShopId(suwonShops.get(0).getId());
        itemService.createItem(request1);

        ItemCreationRequest request2 = ItemCreationRequest.builder()
                .name("kiwi").price(1200L).stock(10L)
                .build();

        request2.setShopId(suwonShops.get(0).getId());
        itemService.createItem(request2);
    }
}
