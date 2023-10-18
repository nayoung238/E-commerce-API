package com.nayoung.orderservice.web.dto;

import com.nayoung.orderservice.domain.OrderItem;
import com.nayoung.orderservice.domain.OrderItemStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.io.Serializable;

@Getter @Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDto implements Serializable {

    private Long id;

    @NotNull
    private Long itemId;

    @NotNull
    @Positive
    private Long quantity;

    @NotNull
    @Min(value = 0)
    private Long price;

    @NotNull
    private Long shopId;

    private OrderItemStatus orderItemStatus;

    public static OrderItemDto fromOrderItem(OrderItem orderItem) {
        return OrderItemDto.builder()
                .id(orderItem.getId())
                .itemId(orderItem.getItemId())
                .quantity(orderItem.getQuantity())
                .price(orderItem.getPrice())
                .shopId(orderItem.getShopId())
                .orderItemStatus((orderItem.getOrderItemStatus() == null) ? OrderItemStatus.WAITING : orderItem.getOrderItemStatus())
                .build();
    }

    public void setOrderStatus(OrderItemStatus orderItemStatus) {
        this.orderItemStatus = orderItemStatus;
    }
}
