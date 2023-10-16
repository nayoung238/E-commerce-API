package com.nayoung.itemservice.domain.item;

import com.nayoung.itemservice.domain.item.log.ItemUpdateLog;
import com.nayoung.itemservice.domain.item.log.ItemUpdateLogRepository;
import com.nayoung.itemservice.exception.ItemException;
import com.nayoung.itemservice.messagequeue.client.OrderItemStatus;
import com.nayoung.itemservice.messagequeue.client.OrderDto;
import com.nayoung.itemservice.web.dto.ItemUpdateLogDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedissonItemService {

    private final RedissonClient redissonClient;
    private final String REDISSON_ITEM_LOCK_PREFIX = "ITEM:";
    private final ItemStockService itemStockService;
    private final ItemUpdateLogRepository itemUpdateLogRepository;
    private final OrderRedisRepository orderRedisRepository;

    /**
     * producer에서 이벤트 유실이라 판단하면 재시도 대상이라 판단해 재전송함
     * 만약 이벤트가 유실되지 않았는데 같은 주문에 대한 이벤트가 재전송되면 consumer는 같은 주문을 중복 처리하게 됨
     * (이벤트 유실에 대한 원인을 제대로 파악할 수 없어서 이미 처리한 이벤트가 재시도 대상이 될 수 있음)
     *
     * 중복 처리를 막기 위해 redis에서 이미 처리된 주문 이벤트인지 먼저 파악 (order ID를 멱등키로 사용)
     */
    @Transactional
    public void updateStock(OrderDto order) {
        String[] redisKey = order.getCreatedAt().toString().split(":");  // key -> order:yyyy-mm-dd'T'HH

        /*
            Redis에서 order:yyyy-mm-dd'T'HH(key)애 orderId(value)의 존재 여부 파악
            addOrderId method로 Redis에 order ID를 추가했을 때 1을 return 받아야 최초 요청
         */
        if(orderRedisRepository.addOrderId(redisKey[0], order.getId()) == 1) {
            List<OrderItemStatus> result = order.getOrderItemDtos().stream()
                    .filter(orderItem -> orderItem.getQuantity() > 0L)
                    .map(orderItem -> updateStock(order.getId(), order.getCustomerAccountId(), orderItem.getItemId(), -orderItem.getQuantity()))
                    .collect(Collectors.toList());

            boolean isAllSucceeded = result.stream()
                    .allMatch(orderItemStatus -> Objects.equals(OrderItemStatus.SUCCEEDED, orderItemStatus));

            if(!isAllSucceeded) undo(order.getId());
        }
    }

    private OrderItemStatus updateStock(Long orderId, Long customerAccountId, Long itemId, Long quantity) {
        RLock lock = redissonClient.getLock(generateKey(itemId));
        try {
            boolean available = lock.tryLock(10, 1, TimeUnit.SECONDS);
            if(!available) {
                log.error("Lock 획득 실패");
                return OrderItemStatus.FAILED;
            }
            ItemUpdateLogDto itemUpdateLogDto = itemStockService.updateStockByRedisson(orderId, customerAccountId, itemId, quantity);
            return itemUpdateLogDto.getOrderItemStatus();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return OrderItemStatus.FAILED;
        } finally {
            lock.unlock();
        }
    }

    private String generateKey(Long key) {
        return REDISSON_ITEM_LOCK_PREFIX + key.toString();
    }

    private void undo(Long orderId) {
        List<ItemUpdateLog> itemUpdateLogs = itemUpdateLogRepository.findAllByOrderId(orderId);
        for(ItemUpdateLog itemUpdateLog : itemUpdateLogs) {
            if(Objects.equals(OrderItemStatus.SUCCEEDED, itemUpdateLog.getOrderItemStatus())) {
                try {
                    updateStock(itemUpdateLog.getOrderId(), itemUpdateLog.getCustomerAccountId(),
                                itemUpdateLog.getItemId(), -itemUpdateLog.getQuantity());
                } catch (ItemException e) {
                    log.error(e.getMessage());
                }
            }
        }
    }
}