package com.ecommerce.orderservice.order.repository;

import com.ecommerce.orderservice.order.entity.Order;
import org.springframework.data.domain.PageRequest;

import java.util.List;

public interface OrderRepositoryCustom {

    List<Order> findByAccountIdOrderByOrderIdDesc(Long accountId, PageRequest pageRequest);

    List<Order> findByAccountIdAndOrderIdLessThanOrderByOrderIdDesc(Long accountId, Long orderId, PageRequest pageRequest);
}
