package com.nayoung.itemservice.kafka.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
public class OrderDto {

    private Long id;
    private String eventId;
    @Setter
    private OrderItemStatus orderStatus;
    @Setter
    private List<OrderItemDto> orderItemDtos;
    private Long customerAccountId;
    private LocalDateTime createdAt;
    private LocalDateTime requestedAt;
}
