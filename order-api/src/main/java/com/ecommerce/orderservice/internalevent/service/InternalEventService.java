package com.ecommerce.orderservice.internalevent.service;

import com.ecommerce.orderservice.common.exception.ErrorCode;
import com.ecommerce.orderservice.common.exception.InternalEventException;
import com.ecommerce.orderservice.internalevent.InternalEventStatus;
import com.ecommerce.orderservice.internalevent.entity.OrderInternalEvent;
import com.ecommerce.orderservice.internalevent.repository.OrderInternalEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InternalEventService {

    private final ApplicationEventPublisher eventPublisher;
    private final OrderInternalEventRepository orderInternalEventRepository;

    @Transactional
    public void publishInternalEvent(OrderInternalEvent orderInternalEvent) {
        eventPublisher.publishEvent(orderInternalEvent);
    }

    @Transactional
    public void saveOrderCreationInternalEvent(OrderInternalEvent event) {
        orderInternalEventRepository.save(event);
    }

    @Transactional
    public void updatePublicationStatus(String orderEventId, InternalEventStatus status) {
        OrderInternalEvent event = orderInternalEventRepository.findByOrderEventId(orderEventId)
                .orElseThrow(() -> new InternalEventException(ErrorCode.NOT_FOUND_ORDER_CREATION_INTERNAL_EVENT));
        event.updatePublicationStatus(status);
    }
}
