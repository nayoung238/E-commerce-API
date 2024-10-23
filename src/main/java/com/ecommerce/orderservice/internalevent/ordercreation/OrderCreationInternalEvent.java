package com.ecommerce.orderservice.internalevent.ordercreation;

import com.ecommerce.orderservice.internalevent.InternalEventStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor
public class OrderCreationInternalEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String orderEventId;

    @Enumerated(EnumType.STRING)
    private InternalEventStatus publicationStatus;

    public void updatePublicationStatus(InternalEventStatus status) {
        this.publicationStatus = status;
    }

    @Builder(access = AccessLevel.PRIVATE)
    private OrderCreationInternalEvent(String orderEventId, InternalEventStatus publicationStatus) {
        this.orderEventId = orderEventId;
        this.publicationStatus = publicationStatus;
    }

    public static OrderCreationInternalEvent init(String orderEventId) {
        return OrderCreationInternalEvent.builder()
                .orderEventId(orderEventId)
                .publicationStatus(InternalEventStatus.init)
                .build();
    }
}
