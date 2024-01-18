package com.nayoung.itemservice.kafka;

//import com.nayoung.itemservice.domain.item.Item;
//import com.nayoung.itemservice.domain.item.repository.ItemRepository;
//import com.nayoung.itemservice.domain.item.service.ItemService;
//import com.nayoung.itemservice.domain.item.RedissonItemService;
//import com.nayoung.itemservice.domain.item.ItemUpdateLog;
//import com.nayoung.itemservice.domain.item.repository.ItemUpdateLogRepository;
//import com.nayoung.itemservice.domain.item.repository.ItemUpdateStatus;
//import com.nayoung.itemservice.domain.shop.ShopRepository;
//import com.nayoung.itemservice.domain.shop.ShopService;
//import com.nayoung.itemservice.exception.ExceptionCode;
//import com.nayoung.itemservice.exception.ItemException;
//import com.nayoung.itemservice.web.dto.ItemDto;
//import com.nayoung.itemservice.web.dto.ItemStockUpdateDto;
//import com.nayoung.itemservice.web.dto.ShopDto;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import java.util.*;
//import java.util.stream.Collectors;
//
//@SpringBootTest
//class KafkaConsumerTest {
//
//    @Autowired
//    private RedissonItemService redissonItemService;
//    @Autowired private ItemService itemService;
//    @Autowired private ItemRepository itemRepository;
//    @Autowired private ItemUpdateLogRepository itemUpdateLogRepository;
//    @Autowired private ShopService shopService;
//    @Autowired private ShopRepository shopRepository;
//
//    private final Long ORDER_ID = 3L;
//    private final Long CUSTOMER_ACCOUNT_ID = 2L;
//    private final String SUWON = "suwon";
//
//    String[] itemName = {"apple", "kiwi"};
//
//    @BeforeEach
//    public void beforeEach() {
//
//        itemRepository.deleteAll();
//        itemUpdateLogRepository.deleteAll();
//        shopRepository.deleteAll();
//
//        ShopDto request = ShopDto.builder()
//                .location(SUWON).name(SUWON + 1).build();
//        ShopDto shopDto = shopService.create(request);
//
//        for (String s : itemName) {
//            ItemDto itemDto = ItemDto.builder()
//                    .name(s).price(1200L)
//                    .stock(20L)
//                    .shopId(shopDto.getId()).build();
//            itemService.create(itemDto);
//        }
//    }
//
//    @AfterEach
//    public void afterEach() {
////        itemRepository.deleteAll();
////        itemUpdateLogRepository.deleteAll();
////        shopRepository.deleteAll();
//    }
//
//    @Test
//    void 모든_상품_재고_충분 () {
//        List<ItemStockUpdateDto> itemStockUpdateDtos = getItemStockToUpdateDtos();
//        Map<Long, Long> previousStockMap = getPreviousStock(itemStockUpdateDtos);
//
//        List<ItemStockUpdateDto> response = itemStockUpdateDtos.parallelStream()
//                .map(i -> redissonItemService.updateStock(i))
//                .collect(Collectors.toList());
//
//        Assertions.assertTrue(response.stream()
//                .allMatch(i -> Objects.equals(ItemUpdateStatus.SUCCEED, i.getItemUpdateStatus())));
//
//        List<ItemUpdateLog> itemUpdateLogs = itemUpdateLogRepository.findAllByOrderId(response.get(0).getOrderId());
//        Assertions.assertTrue(itemUpdateLogs.stream()
//                .allMatch(itemUpdateLog -> itemUpdateLog.getItemUpdateStatus() == ItemUpdateStatus.SUCCEED));
//
//        for(ItemUpdateLog itemUpdateLog : itemUpdateLogs) {
//            Long previousStock = previousStockMap.get(itemUpdateLog.getItemId());
//            Item item = itemRepository.findById(itemUpdateLog.getItemId()).orElseThrow();
//            Assertions.assertEquals(previousStock, item.getStock() + itemUpdateLog.getQuantity());
//        }
//    }
//
//    @Test
//    void 일부_상품_재고_부족 () {
//        List<ItemStockUpdateDto> itemStockUpdateDtos = getItemStockToUpdateDtosByExcessQuantity();
//        Map<Long, Long> previousStockMap = getPreviousStock(itemStockUpdateDtos);
//
//        List<ItemStockUpdateDto> response = itemStockUpdateDtos.stream()
//                .map(i -> redissonItemService.updateStock(i))
//                .collect(Collectors.toList());
//
//        Assertions.assertTrue(response.stream()
//                .noneMatch(r -> Objects.equals(ItemUpdateStatus.SUCCEED, r.getItemUpdateStatus())));
//
//        // Log 확인
//        List<ItemUpdateLog> itemUpdateLogs = itemUpdateLogRepository.findAllByOrderId(response.get(0).getOrderId());
//        Assertions.assertTrue(itemUpdateLogs.stream()
//                .noneMatch(i -> Objects.equals(ItemUpdateStatus.SUCCEED, i.getItemUpdateStatus())));
//        Assertions.assertTrue(itemUpdateLogs.stream().allMatch(i -> i.getQuantity() == 0L));
//
//        for(ItemStockUpdateDto itemStockUpdateDto : itemStockUpdateDtos) {
//            Item item = itemRepository.findById(itemStockUpdateDto.getItemId()).orElseThrow();
//            Assertions.assertEquals(previousStockMap.get(item.getId()), item.getStock());
//        }
//    }
//
//    private List<ItemStockUpdateDto> getItemStockToUpdateDtos() {
//        List<Item> items = itemRepository.findAll();
//        assert(items.size() > 0);
//
//        return items.stream()
//                .map(i -> ItemStockUpdateDto.builder()
//                        .shopId(i.getShop().getId()).orderId(1L)
//                        .itemId(i.getId())
//                        .quantity(i.getStock() / 2).build())
//                .collect(Collectors.toList());
//    }
//
//    private List<ItemStockUpdateDto> getItemStockToUpdateDtosByExcessQuantity() {
//        List<Item> items = itemRepository.findAll();
//        assert(items.size() == 2);
//
//        List<ItemStockUpdateDto> orderItemRequests = new ArrayList<>();
//        orderItemRequests.add(ItemStockUpdateDto.builder()
//                .shopId(items.get(0).getShop().getId())
//                .itemId(items.get(0).getId())
//                .quantity(items.get(0).getStock() / 2).build());
//
//        orderItemRequests.add(ItemStockUpdateDto.builder()
//                .shopId(items.get(1).getShop().getId())
//                .itemId(items.get(1).getId())
//                .quantity(items.get(1).getStock() + 1).build()); // 재고보다 많은 주문
//
//        return orderItemRequests;
//    }
//
//    private Map<Long, Long> getPreviousStock(List<ItemStockUpdateDto> requests) {
//        Map<Long, Long> previousStock = new HashMap<>();
//        for(ItemStockUpdateDto request : requests) {
//            Item item = itemRepository.findById(request.getItemId())
//                    .orElseThrow(() -> new ItemException(ExceptionCode.NOT_FOUND_ITEM));
//            previousStock.put(item.getId(), item.getStock());
//        }
//        return previousStock;
//    }
//}