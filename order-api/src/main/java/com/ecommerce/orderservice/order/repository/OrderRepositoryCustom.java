package com.ecommerce.orderservice.order.repository;

import com.ecommerce.orderservice.order.entity.Order;
import org.springframework.data.domain.PageRequest;

import java.util.List;

public interface OrderRepositoryCustom {

    List<Order> findByUserIdOrderByOrderIdDesc(Long userId, PageRequest pageRequest);

    List<Order> findByUserIdAndOrderIdLessThanOrderByOrderIdDesc(Long userId, Long orderId, PageRequest pageRequest);
}
