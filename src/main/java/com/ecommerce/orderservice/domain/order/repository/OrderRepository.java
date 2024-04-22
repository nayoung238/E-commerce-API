package com.ecommerce.orderservice.domain.order.repository;

import com.ecommerce.orderservice.domain.order.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long>, OrderRepositoryCustom {
    Optional<Order> findByOrderEventId(String orderEventId);
    boolean existsByOrderEventId(String orderEventId);
}
