package com.nayoung.itemservice.domain.item;

import com.nayoung.itemservice.domain.item.log.OrderStatus;
import com.nayoung.itemservice.web.dto.ItemStockUpdateRequest;
import com.nayoung.itemservice.web.dto.ItemStockUpdateResponse;
import com.nayoung.itemservice.web.dto.OrderItemResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service @Slf4j
@RequiredArgsConstructor
public class OrderItemService {

    private final RedissonItemService redissonItemService;
    private final ItemService itemService;

    public ItemStockUpdateResponse updateItemsStock(ItemStockUpdateRequest request) {
        List<CompletableFuture<OrderItemResponse>> result = request.getOrderItemRequests()
                .stream()
                .map(o -> CompletableFuture.supplyAsync(
                        () -> redissonItemService.decreaseItemStock(request.getOrderId(), o)))
                .collect(Collectors.toList());

        List<OrderItemResponse> orderItemResponses = result.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());

        boolean isAllPossible = orderItemResponses.parallelStream().allMatch(r -> r.getOrderStatus() == OrderStatus.SUCCEED);
        if(isAllPossible)
            return ItemStockUpdateResponse.from(true, request, orderItemResponses);

        undo(request.getOrderId(), orderItemResponses);
        return ItemStockUpdateResponse.from(false, request, orderItemResponses);
    }

    public void undo(Long orderId, List<OrderItemResponse> orderItemResponses) {
        itemService.undo(orderId, orderItemResponses);
    }
}
