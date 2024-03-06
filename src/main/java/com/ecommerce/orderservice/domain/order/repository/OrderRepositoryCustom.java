package com.ecommerce.orderservice.domain.repository;

import com.ecommerce.orderservice.domain.Order;
import org.springframework.data.domain.PageRequest;

import java.util.List;

public interface OrderRepositoryCustom {

    List<Order> findByUserIdOrderByOrderIdDesc(Long id, PageRequest pageRequest);

    List<Order> findByUserIdAndOrderIdLessThanOrderByOrderIdDesc(Long id, Long orderId, PageRequest pageRequest);
}
