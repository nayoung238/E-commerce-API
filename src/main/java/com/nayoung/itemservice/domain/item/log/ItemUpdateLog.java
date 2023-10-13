package com.nayoung.itemservice.domain.item.log;

import com.nayoung.itemservice.messagequeue.client.OrderItemStatus;
import lombok.*;

import javax.persistence.*;

@Entity @Getter @Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ItemUpdateLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private OrderItemStatus orderItemStatus;

    private Long orderId;
    private Long customerAccountId;

    private Long itemId;
    private Long quantity;

    public static ItemUpdateLog from(OrderItemStatus orderItemStatus, Long orderId,
                                     Long customerAccountId, Long itemId, Long quantity) {
        return ItemUpdateLog.builder()
                .orderItemStatus(orderItemStatus)
                .orderId(orderId)
                .customerAccountId(customerAccountId)
                .itemId(itemId)
                .quantity(orderItemStatus == OrderItemStatus.OUT_OF_STOCK ? 0 : quantity)
                .build();
    }
}
