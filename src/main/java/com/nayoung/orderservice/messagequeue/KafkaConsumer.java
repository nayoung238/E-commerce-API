package com.nayoung.orderservice.messagequeue;

import com.nayoung.orderservice.domain.OrderService;
import com.nayoung.orderservice.web.dto.OrderDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service @Slf4j
@RequiredArgsConstructor
public class KafkaConsumer {

    private final OrderService orderService;

    /**
     * 주문에 대한 재고 변경 결과(KStream) + waiting 상태의 주문(KTable)을 Join한 결과(주문 상세)를 DB에 insert
     * 1개의 주문 생성에 대해 DB 한 번 접근 (insert)
     */
    @KafkaListener(topics = KStreamKTableJoinConfig.FINAL_ORDER_CREATION_TOPIC_NAME)
    public void createOrderOnDB(OrderDto orderDto) {
        log.info("Consuming message success -> eventId: {}, orderStatus: {}",
                orderDto.getEventId(),
                orderDto.getOrderStatus());

        orderService.insertFinalOrderOnDB(orderDto);
    }

    /**
     * 주문에 대한 '재고 변경 결과 이벤트'를 바탕으로 상태를 update하는 방식
     * 1개의 주문 생성에 대해 DB 두 번 접근 (insert -> update)
     */
    //@KafkaListener(topics = KStreamKTableJoinConfig.ITEM_UPDATE_RESULT_TOPIC_NAME)
    public void updateOrderStatus(OrderDto orderDto) {
        log.info("Consuming message success -> orderId: {}, orderStatus: {}",
                orderDto.getId(),
                orderDto.getOrderStatus());

        orderService.updateOrderStatus(orderDto);
    }
}