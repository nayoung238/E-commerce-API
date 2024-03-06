package com.ecommerce.orderservice.domain.order.repository;

import com.ecommerce.orderservice.domain.order.Order;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserIdAndIdLessThanOrderByIdDesc(Long userId, Long id, Pageable pageable);
    List<Order> findByUserIdOrderByIdDesc(Long userId, Pageable pageable);
    Optional<Order> findByOrderEventKey(String orderEventKey);
    boolean existsByOrderEventKey(String orderEventKey);
}
