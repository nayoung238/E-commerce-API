package com.ecommerce.orderservice.domain.order.dto;

import com.ecommerce.orderservice.domain.order.OrderItem;
import com.ecommerce.orderservice.domain.order.OrderProcessingStatus;
import com.ecommerce.orderservice.kafka.dto.OrderItemKafkaEvent;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
public class OrderItemDto {

    private final Long id;
    private final Long itemId;
    private final Long quantity;
    private OrderProcessingStatus orderProcessingStatus;

    @Builder(access = AccessLevel.PRIVATE)
    private OrderItemDto(Long id, Long itemId, Long quantity, OrderProcessingStatus orderProcessingStatus) {
        this.id = id;
        this.itemId = itemId;
        this.quantity = quantity;
        this.orderProcessingStatus = orderProcessingStatus;
    }

    public static OrderItemDto of(OrderItem orderItem) {
        return OrderItemDto.builder()
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
    public static OrderItemDto of(OrderItemKafkaEvent orderItemKafkaEvent) {
        return OrderItemDto.builder()
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
