package com.nayoung.itemservice.domain.item.log;

import com.nayoung.itemservice.messagequeue.KafkaConsumer;
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
    private ItemUpdateStatus itemUpdateStatus;

    private Long orderId;
    private Long customerAccountId;
    private String createdAt; // order-service 기준

    private Long itemId;
    private Long quantity;

    public static ItemUpdateLog from(ItemUpdateStatus itemUpdateStatus, Long orderId,
                                     Long customerAccountId, KafkaConsumer.ItemStockUpdateDetails request) {
        return ItemUpdateLog.builder()
                .itemUpdateStatus(itemUpdateStatus)
                .orderId(orderId)
                .customerAccountId(customerAccountId)
                .itemId(request.getItemId())
                .quantity(itemUpdateStatus == ItemUpdateStatus.OUT_OF_STOCK ? 0 : request.getQuantity())
                .build();
    }
}
