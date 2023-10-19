package com.nayoung.itemservice.domain.item.log;

import com.nayoung.itemservice.messagequeue.client.OrderItemDto;
import com.nayoung.itemservice.messagequeue.client.OrderItemStatus;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity @Getter @Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ItemUpdateLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String eventId;

    @Enumerated(EnumType.STRING)
    private OrderItemStatus orderItemStatus;

    private Long itemId;
    private Long quantity;
    private LocalDateTime logCreatedAt;

    public static ItemUpdateLog from(OrderItemStatus orderItemStatus, OrderItemDto orderItemDto, String eventId) {
        return ItemUpdateLog.builder()
                .eventId(eventId)
                .orderItemStatus(orderItemStatus)
                .itemId(orderItemDto.getItemId())
                .quantity(orderItemStatus == OrderItemStatus.OUT_OF_STOCK ? 0 : orderItemDto.getQuantity())
                .build();
    }

    public void setLogCreatedAt(LocalDateTime logCreatedAt) {
        this.logCreatedAt = logCreatedAt;
    }
}
