package com.ecommerce.orderservice.internalevent.repository;

import com.ecommerce.orderservice.internalevent.entity.OrderInternalEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderInternalEventRepository extends JpaRepository<OrderInternalEvent, Long> {

    Optional<OrderInternalEvent> findByOrderEventId(String orderEventId);
}
