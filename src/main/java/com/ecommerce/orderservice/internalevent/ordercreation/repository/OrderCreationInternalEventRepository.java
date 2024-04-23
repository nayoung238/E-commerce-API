package com.ecommerce.orderservice.internalevent.ordercreation.repository;

import com.ecommerce.orderservice.internalevent.ordercreation.OrderCreationInternalEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderCreationInternalEventRepository extends JpaRepository<OrderCreationInternalEvent, Long> {

    Optional<OrderCreationInternalEvent> findByOrderEventId(String orderEventId);
}
