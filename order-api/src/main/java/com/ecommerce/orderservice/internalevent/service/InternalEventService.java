package com.ecommerce.orderservice.internalevent.service;

import com.ecommerce.orderservice.common.exception.ExceptionCode;
import com.ecommerce.orderservice.common.exception.InternalEventException;
import com.ecommerce.orderservice.internalevent.InternalEventStatus;
import com.ecommerce.orderservice.internalevent.ordercreation.OrderCreationInternalEvent;
import com.ecommerce.orderservice.internalevent.ordercreation.repository.OrderCreationInternalEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InternalEventService {

    private final ApplicationEventPublisher eventPublisher;
    private final OrderCreationInternalEventRepository orderCreationInternalEventRepository;

    @Transactional
    public void publishInternalEvent(OrderCreationInternalEvent orderCreationInternalEvent) {
        eventPublisher.publishEvent(orderCreationInternalEvent);
    }

    @Transactional
    public void saveOrderCreationInternalEvent(OrderCreationInternalEvent event) {
        orderCreationInternalEventRepository.save(event);
    }

    @Transactional
    public void updatePublicationStatus(String orderEventId, InternalEventStatus status) {
        OrderCreationInternalEvent event = orderCreationInternalEventRepository.findByOrderEventId(orderEventId)
                .orElseThrow(() -> new InternalEventException(ExceptionCode.NOT_FOUND_ORDER_CREATION_INTERNAL_EVENT));
        event.updatePublicationStatus(status);
    }
}
