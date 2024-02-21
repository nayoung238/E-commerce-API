package com.ecommerce.orderservice.domain.repository;

import com.ecommerce.orderservice.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long>, OrderRepositoryCustom {

    Optional<Order> findByEventId(String eventId);

    boolean existsByEventId(String eventId);
}
