package com.nayoung.accountservice.web.dto;

import com.nayoung.accountservice.client.OrderStatus;
import lombok.Data;

@Data
public class OrderItemResponse {
    private Long orderItemId;
    private Long itemId;
    private Long quantity;
    private Long price;
    private Long shopId;
    private OrderStatus orderStatus;
}
