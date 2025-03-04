package com.ecommerce.orderservice.order.repository;

import com.ecommerce.orderservice.order.entity.Order;
import com.ecommerce.orderservice.order.entity.QOrder;
import com.ecommerce.orderservice.order.entity.QOrderItem;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;

import java.util.List;

@RequiredArgsConstructor
public class OrderRepositoryCustomImpl implements OrderRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Order> findByUserIdOrderByOrderIdDesc(Long userId, PageRequest pageRequest) {
        return queryFactory.selectFrom(QOrder.order)
                .where(QOrder.order.userId.eq(userId))
                .join(QOrder.order.orderItems, QOrderItem.orderItem).fetchJoin()
                .orderBy(new OrderSpecifier<>(com.querydsl.core.types.Order.DESC, QOrder.order.id))
                .limit(pageRequest.getPageSize())
                .fetch();
    }

    @Override
    public List<Order> findByUserIdAndOrderIdLessThanOrderByOrderIdDesc(Long userId, Long orderId, PageRequest pageRequest) {
        return queryFactory.selectFrom(QOrder.order)
                .where(
                        QOrder.order.userId.eq(userId),
                        QOrder.order.id.lt(orderId)
                )
                .join(QOrder.order.orderItems, QOrderItem.orderItem).fetchJoin()
                .orderBy(QOrder.order.id.desc())
                .limit(pageRequest.getPageSize())
                .fetch();
    }
}
