package com.nayoung.itemservice.domain;

import com.nayoung.itemservice.web.dto.ItemCreationRequest;
import com.nayoung.itemservice.web.dto.ItemInfoUpdateRequest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
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

    private static final int threadCount = 5;

    @BeforeEach
    public void beforeEach() {
        ItemCreationRequest request = new ItemCreationRequest();
        request.setName("apple");
        request.setPrice(1300L);
        request.setStock(0L);

        itemService.createItem(request);
    }

    @AfterEach
    public void afterEach() {
        itemRepository.deleteAll();
    }

    @Test
    @DisplayName("Lost Update가 발생하지 않는지 테스트")
    public void stockUpdateTest() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        CountDownLatch countDownLatch = new CountDownLatch(threadCount);
        List<ItemInfoUpdateRequest> requestList = getIItemUpdateInfoList();

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

        Item item = itemRepository.findById(1L).orElseThrow();
        Assertions.assertEquals(totalQuantity , item.getStock());
    }

    private List<ItemInfoUpdateRequest> getIItemUpdateInfoList() {
        List<ItemInfoUpdateRequest> itemInfoUpdateRequestList = new ArrayList<>();
        for(int i = 0; i < ItemServiceTest.threadCount; i++)
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
