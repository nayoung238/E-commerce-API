package com.ecommerce.orderservice.internalevent.entity;

import com.ecommerce.orderservice.internalevent.InternalEventStatus;
import com.ecommerce.orderservice.order.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderInternalEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @Column(nullable = false, unique = true)
    private String orderEventId;

    @Enumerated(EnumType.STRING)
    private InternalEventStatus publicationStatus;

    private OrderStatus orderStatus;

    public void updatePublicationStatus(InternalEventStatus status) {
        this.publicationStatus = status;
    }

    public static OrderInternalEvent of(Long userId, String orderEventId, OrderStatus orderStatus) {
        return OrderInternalEvent.builder()
                .userId(userId)
                .orderEventId(orderEventId)
                .publicationStatus(InternalEventStatus.init)
                .orderStatus(orderStatus)
                .build();
    }
}
