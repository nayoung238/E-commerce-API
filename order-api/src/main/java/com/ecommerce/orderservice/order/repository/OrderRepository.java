package com.ecommerce.orderservice.order.repository;

import com.ecommerce.orderservice.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long>, OrderRepositoryCustom {

    List<Order> findAllByUserId(long userId);

    @Query("select o from Order o where o.userId=:userId order by o.id desc limit 1")
    Optional<Order> findLatestOrderByUserId(@Param("userId") long userId);

    Optional<Order> findByOrderEventId(String orderEventId);

    boolean existsByOrderEventId(String orderEventId);
}
