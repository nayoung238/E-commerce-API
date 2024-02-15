package com.ecommerce.accountservice.web.dto;

import com.ecommerce.accountservice.openfeign.client.OrderDto;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class OrderListDto {

    private Long userId;

    private List<OrderDto> orderDtoList;

    public static OrderListDto fromUserIdAndOrderDtoList(Long userId, List<OrderDto> orderDtoList) {
        return OrderListDto.builder()
                .userId(userId)
                .orderDtoList(orderDtoList)
                .build();
    }
}
