package com.nayoung.itemservice.domain.item.log;

import com.nayoung.itemservice.web.dto.ItemStockUpdateDto;
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
    private ItemUpdateStatus itemUpdateStatus;

    private Long orderId;
    private Long shopId;
    private Long itemId;
    private Long quantity;

    @Builder
    private ItemUpdateLog(ItemUpdateStatus itemUpdateStatus, Long orderId, Long shopId, Long itemId, Long quantity) {
        this.itemUpdateStatus = itemUpdateStatus;
        this.orderId = orderId;
        this.shopId = shopId;
        this.itemId = itemId;
        this.quantity = quantity;
    }

    public static ItemUpdateLog from(ItemUpdateStatus itemUpdateStatus, Long orderId, ItemStockUpdateDto request) {
        return ItemUpdateLog.builder()
                .itemUpdateStatus(itemUpdateStatus)
                .orderId(orderId)
                .shopId(request.getShopId())
                .itemId(request.getItemId())
                .quantity(itemUpdateStatus == ItemUpdateStatus.OUT_OF_STOCK ? 0 : request.getQuantity())
                .build();
    }

    public void setItemUpdateStatus(ItemUpdateStatus itemUpdateStatus) {
        this.itemUpdateStatus = itemUpdateStatus;
    }

    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }
}
