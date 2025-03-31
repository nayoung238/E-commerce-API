package com.ecommerce.orderservice.order.dto.response;

import com.ecommerce.orderservice.order.entity.OrderItem;
import com.ecommerce.orderservice.order.enums.OrderStatus;
import com.ecommerce.orderservice.kafka.dto.OrderItemKafkaEvent;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
public class OrderItemResponse {

    private final Long id;
    private final Long itemId;
    private final Long quantity;
    private OrderStatus orderStatus;

    @Builder(access = AccessLevel.PRIVATE)
    private OrderItemResponse(Long id, Long itemId, Long quantity, OrderStatus orderStatus) {
        this.id = id;
        this.itemId = itemId;
        this.quantity = quantity;
        this.orderStatus = orderStatus;
    }

    public static OrderItemResponse of(OrderItem orderItem) {
        return OrderItemResponse.builder()
                .id(orderItem.getId())
                .itemId(orderItem.getItemId())
                .quantity(orderItem.getQuantity())
                .orderStatus((orderItem.getOrderStatus() == null) ? OrderStatus.PROCESSING : orderItem.getOrderStatus())
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
                .orderStatus(orderItemKafkaEvent.getOrderStatus())
                .build();
    }

    public void updateStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }
}
