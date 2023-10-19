package com.nayoung.itemservice.domain.item;

import com.nayoung.itemservice.domain.item.log.ItemUpdateLog;
import com.nayoung.itemservice.domain.item.log.ItemUpdateLogRepository;
import com.nayoung.itemservice.exception.ExceptionCode;
import com.nayoung.itemservice.exception.ItemException;
import com.nayoung.itemservice.exception.StockException;
import com.nayoung.itemservice.messagequeue.client.OrderItemDto;
import com.nayoung.itemservice.messagequeue.client.OrderItemStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockUpdateByPessimisticLock implements StockUpdate {

    private final ItemRepository itemRepository;
    private final ItemUpdateLogRepository itemUpdateLogRepository;

    /**
     * Exclusive Lock 사용
     * 모든 요청이 X-Lock을 획득하기 위해 대기 -> 대기로 인해 지연 시간 발생
     * 재고 변경 작업의 지연은 주문 상태 확정의 지연으로 이어짐
     *
     * DB X-Lock을 획득한 노드가 죽는 경우 락을 자동 반납하지 않아 다른 요청은 무한정 대기할 수 있음
     * -> Redis Distributed lock에 lease time 설정하는 방식으로 해결 (updateStockByRedisson method)
     */
    @Override
    public OrderItemDto updateStock(OrderItemDto orderItemDto, String eventId) {
        OrderItemStatus orderItemStatus;
        try {
            Item item = itemRepository.findByIdWithPessimisticLock(orderItemDto.getItemId())
                    .orElseThrow(() -> new ItemException(ExceptionCode.NOT_FOUND_ITEM));
            item.updateStock(orderItemDto.getQuantity());
            orderItemStatus = OrderItemStatus.SUCCEEDED;
        } catch (ItemException e) {
            orderItemStatus = OrderItemStatus.FAILED;
        } catch(StockException e) {
            orderItemStatus = OrderItemStatus.OUT_OF_STOCK;
        }

        ItemUpdateLog itemUpdateLog = ItemUpdateLog.from(orderItemStatus, orderItemDto, eventId);
        itemUpdateLogRepository.save(itemUpdateLog);

        orderItemDto.setOrderItemStatus(orderItemStatus);
        return orderItemDto;
    }
}
