package com.ecommerce.orderservice.order.dto.response;

import com.ecommerce.orderservice.order.entity.OrderItem;
import com.ecommerce.orderservice.order.enums.OrderProcessingStatus;
import com.ecommerce.orderservice.kafka.dto.OrderItemKafkaEvent;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
public class OrderItemResponse {

    private final Long id;
    private final Long itemId;
    private final Long quantity;
    private OrderProcessingStatus orderProcessingStatus;

    @Builder(access = AccessLevel.PRIVATE)
    private OrderItemResponse(Long id, Long itemId, Long quantity, OrderProcessingStatus orderProcessingStatus) {
        this.id = id;
        this.itemId = itemId;
        this.quantity = quantity;
        this.orderProcessingStatus = orderProcessingStatus;
    }

    public static OrderItemResponse of(OrderItem orderItem) {
        return OrderItemResponse.builder()
                .id(orderItem.getId())
                .itemId(orderItem.getItemId())
                .quantity(orderItem.getQuantity())
                .orderProcessingStatus((orderItem.getOrderProcessingStatus() == null) ?
                        OrderProcessingStatus.PROCESSING
                        : orderItem.getOrderProcessingStatus())
                .build();
    }

    /**
     * KStream-KTable Join으로 주문 생성하는 방식에서 사용
     * @param orderItemKafkaEvent DB insert 전이라 id 설정 안됨 (OrderItemDto.id null로 설정)
     * @return
     */
    public static OrderItemResponse of(OrderItemKafkaEvent orderItemKafkaEvent) {
        return OrderItemResponse.builder()
                .id(null)
                .itemId(orderItemKafkaEvent.getItemId())
                .quantity(orderItemKafkaEvent.getQuantity())
                .orderProcessingStatus(orderItemKafkaEvent.getOrderProcessingStatus())
                .build();
    }

    public void updateStatus(OrderProcessingStatus orderProcessingStatus) {
        this.orderProcessingStatus = orderProcessingStatus;
    }
}
