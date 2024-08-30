package com.ecommerce.orderservice.domain.order.dto;

import com.ecommerce.orderservice.domain.order.OrderItem;
import com.ecommerce.orderservice.domain.order.OrderStatus;
import com.ecommerce.orderservice.kafka.dto.OrderItemKafkaEvent;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderItemDto {

    private Long id;

    private Long itemId;

    private Long quantity;

    private OrderStatus orderStatus;

    public static OrderItemDto of(OrderItem orderItem) {
        return OrderItemDto.builder()
                .id(orderItem.getId())
                .itemId(orderItem.getItemId())
                .quantity(orderItem.getQuantity())
                .orderStatus((orderItem.getOrderStatus() == null) ? OrderStatus.WAITING : orderItem.getOrderStatus())
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
                .orderStatus(orderItemKafkaEvent.getOrderStatus())
                .build();
    }
}
