package com.nayoung.orderservice.web.dto;

import com.nayoung.orderservice.domain.Order;
import com.nayoung.orderservice.domain.OrderStatus;
import lombok.Builder;
import lombok.Getter;

import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.stream.Collectors;

@Getter @Builder
public class OrderDto {

    private Long orderId;
    private OrderStatus orderStatus;

    @NotBlank
    private List<OrderItemDto> orderItemDtos;
    private Long totalPrice;

    @NotBlank
    private Long customerAccountId;

    public static OrderDto fromOrder(Order order) {
        List<OrderItemDto> orderItemDtos = order.getOrderItems().parallelStream()
                .map(OrderItemDto::fronmOrderItem)
                .collect(Collectors.toList());

        for(OrderItemDto orderItemDto : orderItemDtos)
            orderItemDto.setOrderStatus(OrderStatus.WAITING);

        return OrderDto.builder()
                .orderId(order.getId())
                .orderStatus(OrderStatus.WAITING)
                .orderItemDtos(orderItemDtos)
                .totalPrice(getTotalPrice(orderItemDtos))
                .customerAccountId(order.getCustomerAccountId())
                .build();
    }

    public static OrderDto fromFailedOrder(List<OrderItemDto> outOfStockItems) {
        for(OrderItemDto orderItemDto : outOfStockItems)
            orderItemDto.setOrderStatus(OrderStatus.OUT_OF_STOCK);

        return OrderDto.builder()
                .orderStatus(OrderStatus.FAILED)
                .orderItemDtos(outOfStockItems)
                .build();
    }

    private static Long getTotalPrice(List<OrderItemDto> orderItemDtos) {
        assert(!orderItemDtos.isEmpty());
        return orderItemDtos.stream()
                .map(OrderItemDto::getPrice)
                .reduce(Long::sum).get();
    }
}
