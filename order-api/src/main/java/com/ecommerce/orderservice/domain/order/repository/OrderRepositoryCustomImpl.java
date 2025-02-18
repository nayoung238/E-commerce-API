package com.ecommerce.orderservice.domain.order.repository;

import com.ecommerce.orderservice.domain.order.QOrder;
import com.ecommerce.orderservice.domain.order.QOrderItem;
import com.ecommerce.orderservice.domain.order.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;

import java.util.List;

@RequiredArgsConstructor
public class OrderRepositoryCustomImpl implements OrderRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Order> findByAccountIdOrderByOrderIdDesc(Long accountId, PageRequest pageRequest) {
        return queryFactory.selectFrom(QOrder.order)
                .where(QOrder.order.accountId.eq(accountId))
                .join(QOrder.order.orderItems, QOrderItem.orderItem).fetchJoin()
                .orderBy(new OrderSpecifier<>(com.querydsl.core.types.Order.DESC, QOrder.order.id))
                .limit(pageRequest.getPageSize())
                .fetch();
    }

    @Override
    public List<Order> findByAccountIdAndOrderIdLessThanOrderByOrderIdDesc(Long accountId, Long orderId, PageRequest pageRequest) {
        return queryFactory.selectFrom(QOrder.order)
                .where(
                        QOrder.order.accountId.eq(accountId),
                        QOrder.order.id.lt(orderId)
                )
                .join(QOrder.order.orderItems, QOrderItem.orderItem).fetchJoin()
                .orderBy(QOrder.order.id.desc())
                .limit(pageRequest.getPageSize())
                .fetch();
    }
}
