package com.nayoung.itemservice.domain.item.service;

import com.nayoung.itemservice.domain.item.Item;
import com.nayoung.itemservice.domain.item.repository.ItemRepository;
import com.nayoung.itemservice.exception.ExceptionCode;
import com.nayoung.itemservice.exception.ItemException;
import com.nayoung.itemservice.exception.StockException;
import com.nayoung.itemservice.kafka.dto.OrderItemDto;
import com.nayoung.itemservice.kafka.dto.OrderItemStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Exclusive Lock 사용
 * 모든 요청이 X-Lock을 획득하기 위해 대기 -> 지연 시간 발생
 * 재고 변경 작업의 지연은 주문 생성 지연으로 이어짐
 *
 * DB X-Lock을 획득한 노드가 죽는 경우 락을 자동 반납하지 않음
 * -> 같은 데이터를 수정하는 다른 요청은 무한 대기하는 문제 발생
 * -> Redis Distributed lock에 lease time 설정해 문제 해결
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class StockUpdateByPessimisticLock implements StockUpdate {

    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public OrderItemDto updateStock(OrderItemDto orderItemDto, String eventId) {
        try {
            Item item = itemRepository.findByIdWithPessimisticLock(orderItemDto.getItemId())
                    .orElseThrow(() -> new ItemException(ExceptionCode.NOT_FOUND_ITEM));

            item.updateStock(orderItemDto.getQuantity());

            orderItemDto.setOrderItemStatus((orderItemDto.getQuantity() < 0) ?
                    OrderItemStatus.SUCCEEDED  // consumption
                    : OrderItemStatus.CANCELED); // production (undo)
        } catch (ItemException e) {
            orderItemDto.setOrderItemStatus(OrderItemStatus.FAILED);
        } catch(StockException e) {
            orderItemDto.setOrderItemStatus(OrderItemStatus.OUT_OF_STOCK);
        }
        return orderItemDto;
    }
}
