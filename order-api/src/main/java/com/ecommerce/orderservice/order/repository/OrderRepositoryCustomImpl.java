package com.ecommerce.orderservice.order.repository;

import com.ecommerce.orderservice.order.entity.Order;
import com.ecommerce.orderservice.order.entity.QOrder;
import com.ecommerce.orderservice.order.entity.QOrderItem;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;

import java.util.List;

@RequiredArgsConstructor
public class OrderRepositoryCustomImpl implements OrderRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Order> findOrdersWithCursor(Long userId, Long orderId, PageRequest pageRequest) {
        BooleanBuilder where = new BooleanBuilder();
        where.and(QOrder.order.userId.eq(userId));

        if (orderId != null) {
            where.and(QOrder.order.id.lt(orderId));
        }

        return queryFactory.selectFrom(QOrder.order)
                .where(where)
                .join(QOrder.order.orderItems, QOrderItem.orderItem).fetchJoin()
                .orderBy(QOrder.order.id.desc())
                .limit(pageRequest.getPageSize())
                .fetch();
    }
}
