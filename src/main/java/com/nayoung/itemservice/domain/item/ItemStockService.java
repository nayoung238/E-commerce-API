package com.nayoung.itemservice.domain.item;

import com.nayoung.itemservice.domain.item.log.ItemUpdateLog;
import com.nayoung.itemservice.domain.item.log.ItemUpdateLogRepository;
import com.nayoung.itemservice.domain.item.log.ItemUpdateStatus;
import com.nayoung.itemservice.exception.ExceptionCode;
import com.nayoung.itemservice.exception.ItemException;
import com.nayoung.itemservice.exception.StockException;
import com.nayoung.itemservice.messagequeue.KafkaConsumer;
import com.nayoung.itemservice.web.dto.ItemUpdateLogDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ItemStockService {

    private final ItemRepository itemRepository;
    private final ItemRedisRepository itemRedisRepository;
    private final ItemUpdateLogRepository itemUpdateLogRepository;

    /**
     * Exclusive Lock 사용
     * 모든 요청이 X-Lock을 획득하기 위해 대기 -> 대기로 인해 지연 시간 발생
     * 재고 변경 작업의 지연은 주문 상태 확정의 지연으로 이어짐
     *
     * DB X-Lock을 획득한 노드가 죽는 경우 락을 자동 반납하지 않아 다른 요청은 무한정 대기할 수 있음
     * -> Redis Distributed lock에 lease time 설정하는 방식으로 해결 (updateStockByRedisson method)
     */
    @Transactional
    public ItemUpdateLogDto decreaseStockByPessimisticLock(Long orderId, Long customerAccountId, KafkaConsumer.ItemStockUpdateDetails request) {
        ItemUpdateStatus itemUpdateStatus;
        try {
            Item item = itemRepository.findByIdWithPessimisticLock(request.getItemId())
                    .orElseThrow(() -> new ItemException(ExceptionCode.NOT_FOUND_ITEM));
            item.decreaseStock(request.getQuantity());
            itemUpdateStatus = ItemUpdateStatus.SUCCEEDED;
        } catch (ItemException e) {
            itemUpdateStatus = ItemUpdateStatus.FAILED;
        } catch(StockException e) {
            itemUpdateStatus = ItemUpdateStatus.OUT_OF_STOCK;
        }
        ItemUpdateLog itemUpdateLog = ItemUpdateLog.from(itemUpdateStatus, orderId, customerAccountId, request);
        itemUpdateLogRepository.save(itemUpdateLog);
        return ItemUpdateLogDto.fromItemUpdateLog(itemUpdateLog);
    }

    /**
     * Redis Distributed Lock + Optimistic Lock 사용
     * Redis 분산락을 획득한 요청만이 DB에 접근해 수정할 수 있음
     * 분산락에 lease time 설정 -> DB 락을 획득한 노드가 죽는 경우 발생할 수 있는 문제 해결 (활성 상태가 될 때까지 모든 요청 대기해야 하는 문제)
     *
     * 분산락 lease time 보다 transaction 처리가 더 길다면 동시성 문제 발생할 수 있음 (여러 요청이 자신이 분산락 주인이라고 착각하고 쿼리 날리는 경우)
     * -> Optimistic Lock을 사용해 DB 반영 시 충돌 감지해 동시성 문제 해결
     */
    @Transactional
    public ItemUpdateLogDto updateStockByRedisson(Long orderId, Long customerAccountId, KafkaConsumer.ItemStockUpdateDetails request) {
        Item item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new ItemException(ExceptionCode.NOT_FOUND_ITEM));

        // Redis에서 재고 차감 시도
        ItemUpdateStatus itemUpdateStatus;
        if(isUpdatableStockByRedis(item.getId(), request.getQuantity()))
            itemUpdateStatus = (request.getQuantity() >= 0) ? ItemUpdateStatus.SUCCEEDED : ItemUpdateStatus.CANCELED;
        else itemUpdateStatus = ItemUpdateStatus.OUT_OF_STOCK;

        ItemUpdateLog itemUpdateLog = ItemUpdateLog.from(itemUpdateStatus, orderId, customerAccountId, request);
        itemUpdateLogRepository.save(itemUpdateLog);
        return ItemUpdateLogDto.fromItemUpdateLog(itemUpdateLog);
    }

    private boolean isUpdatableStockByRedis(Long itemId, Long quantity) {
        Long stock = itemRedisRepository.decrementItemStock(itemId, quantity);
        if(stock >= 0) return true;

        // undo
        itemRedisRepository.incrementItemStock(itemId, quantity);
        return false;
    }
}
