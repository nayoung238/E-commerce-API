package com.ecommerce.orderservice.domain.order.repository;

import com.ecommerce.orderservice.domain.order.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long>, OrderRepositoryCustom {

    List<Order> findAllByAccountId(long accountId);

    @Query("select o from Order o where o.accountId=:accountId order by o.id desc limit 1")
    Optional<Order> findLatestOrderByAccountId(@Param("accountId") long accountId);

    Optional<Order> findByOrderEventId(String orderEventId);

    boolean existsByOrderEventId(String orderEventId);
}
