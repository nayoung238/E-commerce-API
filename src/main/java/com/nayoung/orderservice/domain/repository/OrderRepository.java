package com.nayoung.orderservice.domain.repository;

import com.nayoung.orderservice.domain.Order;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserIdAndIdLessThanOrderByIdDesc(Long userId, Long id, Pageable pageable);
    List<Order> findByUserIdOrderByIdDesc(Long userId, Pageable pageable);
    Optional<Order> findByEventId(String eventId);

    boolean existsByEventId(String eventId);
}
