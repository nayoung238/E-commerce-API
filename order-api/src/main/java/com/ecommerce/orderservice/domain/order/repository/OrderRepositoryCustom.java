package com.ecommerce.orderservice.domain.order.repository;

import com.ecommerce.orderservice.domain.order.Order;
import org.springframework.data.domain.PageRequest;

import java.util.List;

public interface OrderRepositoryCustom {

    List<Order> findByAccountIdOrderByOrderIdDesc(Long accountId, PageRequest pageRequest);

    List<Order> findByAccountIdAndOrderIdLessThanOrderByOrderIdDesc(Long accountId, Long orderId, PageRequest pageRequest);
}
