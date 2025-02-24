package com.ecommerce.orderservice.internalevent.order.repository;

import com.ecommerce.orderservice.internalevent.order.event.OrderInternalEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderInternalEventRepository extends JpaRepository<OrderInternalEvent, Long> {

    Optional<OrderInternalEvent> findByOrderEventId(String orderEventId);
}
