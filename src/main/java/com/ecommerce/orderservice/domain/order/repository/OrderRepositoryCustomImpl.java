package com.ecommerce.orderservice.domain.repository;

import com.ecommerce.orderservice.domain.Order;
import com.ecommerce.orderservice.domain.QOrder;
import com.ecommerce.orderservice.domain.QOrderItem;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;

import java.util.List;

@RequiredArgsConstructor
public class OrderRepositoryCustomImpl implements OrderRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Order> findByUserIdOrderByOrderIdDesc(Long id, PageRequest pageRequest) {
        return queryFactory.selectFrom(QOrder.order)
                .join(QOrder.order.orderItems, QOrderItem.orderItem).fetchJoin()
                .orderBy(new OrderSpecifier<>(com.querydsl.core.types.Order.DESC, QOrder.order.id))
                .limit(pageRequest.getPageSize())
                .fetch();
    }

    @Override
    public List<Order> findByUserIdAndOrderIdLessThanOrderByOrderIdDesc(Long id, Long orderId, PageRequest pageRequest) {
        return queryFactory.selectFrom(QOrder.order)
                .join(QOrder.order.orderItems, QOrderItem.orderItem).fetchJoin()
                .orderBy(new OrderSpecifier<>(com.querydsl.core.types.Order.DESC, QOrder.order.id))
                .offset(orderId)
                .limit(pageRequest.getPageSize())
                .fetch();
    }
}
