package com.nayoung.orderservice.web.dto;

import com.nayoung.orderservice.domain.Order;
import com.nayoung.orderservice.domain.OrderItemStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter @Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto implements Serializable {

    private Long id;
    private OrderItemStatus orderStatus;

    @NotNull
    @Valid
    private List<OrderItemDto> orderItemDtos;

    @NotNull
    @Min(value = 1)
    private Long customerAccountId;
    private Long totalPrice;
    private LocalDateTime createdAt;

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
                .createdAt(order.getCreatedAt())
                .build();
    }

    public void setOrderStatus(OrderItemStatus orderItemStatus) {
        this.orderStatus = orderItemStatus;
    }
}
