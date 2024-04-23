package com.ecommerce.orderservice.internalevent.ordercreation;

import com.ecommerce.orderservice.internalevent.InternalEventStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreationInternalEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String orderEventId;

    @Enumerated(EnumType.STRING)
    private InternalEventStatus publicationStatus;

    public void updatePublicationStatus(InternalEventStatus status) {
        this.publicationStatus = status;
    }
}
