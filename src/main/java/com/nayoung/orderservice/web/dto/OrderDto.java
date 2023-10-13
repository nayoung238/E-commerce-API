package com.nayoung.orderservice.web.dto;

import com.nayoung.orderservice.domain.Order;
import com.nayoung.orderservice.domain.OrderItemStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

@Getter @Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto implements Serializable {

    private Long id;
    private OrderItemStatus orderStatus;

    @NotBlank
    private List<OrderItemDto> orderItemDtos;

    @NotBlank
    private Long customerAccountId;
    private Long totalPrice;
    private String createdAt;

    public static OrderDto fromOrder(Order order) {
        List<OrderItemDto> orderItemDtos = order.getOrderItems().parallelStream()
                .map(OrderItemDto::fromOrderItem)
                .collect(Collectors.toList());

        for(OrderItemDto orderItemDto : orderItemDtos)
            orderItemDto.setOrderStatus(OrderItemStatus.WAITING);

        return OrderDto.builder()
                .id(order.getId())
                .orderStatus(OrderItemStatus.WAITING)
                .orderItemDtos(orderItemDtos)
                .totalPrice(order.getTotalPrice())
                .customerAccountId(order.getCustomerAccountId())
                .createdAt(order.getCreatedAt().toString())
                .build();
    }

    public static OrderDto fromFailedOrder(List<OrderItemDto> outOfStockItems) {
        for(OrderItemDto orderItemDto : outOfStockItems)
            orderItemDto.setOrderStatus(OrderItemStatus.OUT_OF_STOCK);

        return OrderDto.builder()
                .orderStatus(OrderItemStatus.FAILED)
                .orderItemDtos(outOfStockItems)
                .build();
    }
}
