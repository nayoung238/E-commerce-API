package com.ecommerce.orderservice.internalevent.order;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QOrderCreationInternalEvent is a Querydsl query type for OrderCreationInternalEvent
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QOrderInternalEvent extends EntityPathBase<OrderCreationInternalEvent> {

    private static final long serialVersionUID = 1185810862L;

    public static final QOrderInternalEvent orderInternalEvent = new QOrderInternalEvent("orderInternalEvent");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath orderEventId = createString("orderEventId");

    public final EnumPath<com.ecommerce.orderservice.internalevent.InternalEventStatus> publicationStatus = createEnum("publicationStatus", com.ecommerce.orderservice.internalevent.InternalEventStatus.class);

    public QOrderInternalEvent(String variable) {
        super(OrderInternalEvent.class, forVariable(variable));
    }

    public QOrderInternalEvent(Path<? extends OrderInternalEvent> path) {
        super(path.getType(), path.getMetadata());
    }

    public QOrderInternalEvent(PathMetadata metadata) {
        super(OrderInternalEvent.class, metadata);
    }

}

