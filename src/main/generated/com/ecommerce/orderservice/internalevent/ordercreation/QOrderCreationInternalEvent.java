package com.ecommerce.orderservice.internalevent.ordercreation;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QOrderCreationInternalEvent is a Querydsl query type for OrderCreationInternalEvent
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QOrderCreationInternalEvent extends EntityPathBase<OrderCreationInternalEvent> {

    private static final long serialVersionUID = 1185810862L;

    public static final QOrderCreationInternalEvent orderCreationInternalEvent = new QOrderCreationInternalEvent("orderCreationInternalEvent");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath orderEventId = createString("orderEventId");

    public final EnumPath<com.ecommerce.orderservice.internalevent.InternalEventStatus> publicationStatus = createEnum("publicationStatus", com.ecommerce.orderservice.internalevent.InternalEventStatus.class);

    public QOrderCreationInternalEvent(String variable) {
        super(OrderCreationInternalEvent.class, forVariable(variable));
    }

    public QOrderCreationInternalEvent(Path<? extends OrderCreationInternalEvent> path) {
        super(path.getType(), path.getMetadata());
    }

    public QOrderCreationInternalEvent(PathMetadata metadata) {
        super(OrderCreationInternalEvent.class, metadata);
    }

}

