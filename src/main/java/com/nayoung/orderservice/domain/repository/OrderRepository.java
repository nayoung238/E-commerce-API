package com.nayoung.orderservice.domain.repository;

import com.nayoung.orderservice.domain.Order;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomerAccountIdAndIdLessThanOrderByIdDesc(Long customerAccountId, Long id, Pageable pageable);
    List<Order> findByCustomerAccountIdOrderByIdDesc(Long customerAccountId, Pageable pageable);
    Optional<Order> findByEventId(String eventId);

    boolean existsByEventId(String eventId);
}
