package com.ecommerce.orderservice.internalevent.order.event;

import com.ecommerce.orderservice.internalevent.InternalEventStatus;
import com.ecommerce.orderservice.order.enums.OrderProcessingStatus;
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

    private Long accountId;

    @Column(nullable = false, unique = true)
    private String orderEventId;

    @Enumerated(EnumType.STRING)
    private InternalEventStatus publicationStatus;

    private OrderProcessingStatus orderProcessingStatus;

    public void updatePublicationStatus(InternalEventStatus status) {
        this.publicationStatus = status;
    }

    public static OrderInternalEvent of(Long accountId, String orderEventId, OrderProcessingStatus orderProcessingStatus) {
        return OrderInternalEvent.builder()
                .accountId(accountId)
                .orderEventId(orderEventId)
                .publicationStatus(InternalEventStatus.init)
                .orderProcessingStatus(orderProcessingStatus)
                .build();
    }
}
