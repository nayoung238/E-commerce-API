package com.nayoung.itemservice.domain.item.log;

import com.nayoung.itemservice.web.dto.ItemStockToUpdateDto;
import lombok.*;

import javax.persistence.*;

@Entity @Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ItemUpdateLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    private Long orderId;
    private Long shopId;
    private Long itemId;
    private Long quantity;

    @Builder
    private ItemUpdateLog(OrderStatus orderStatus, Long orderId, Long shopId, Long itemId, Long quantity) {
        this.orderStatus = orderStatus;
        this.orderId = orderId;
        this.shopId = shopId;
        this.itemId = itemId;
        this.quantity = quantity;
    }

    public static ItemUpdateLog from(OrderStatus orderStatus, Long orderId, ItemStockToUpdateDto request) {
        return ItemUpdateLog.builder()
                .orderStatus(orderStatus)
                .orderId(orderId)
                .shopId(request.getShopId())
                .itemId(request.getItemId())
                .quantity(request.getQuantity())
                .build();
    }

    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }
}
