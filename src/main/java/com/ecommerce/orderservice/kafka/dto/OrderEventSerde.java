package com.ecommerce.orderservice.kafka.dto;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

public class OrderEventSerde implements Serde<OrderEvent> {

    private final OrderEventSerializer orderEventSerializer = new OrderEventSerializer();
    private final OrderEventDeserializer orderEventDeserializer = new OrderEventDeserializer();

    public OrderEventSerde() {}

    @Override
    public void close() {
        Serde.super.close();
        orderEventSerializer.close();
        orderEventDeserializer.close();
    }

    @Override
    public Serializer<OrderEvent> serializer() {
        return orderEventSerializer;
    }

    @Override
    public Deserializer<OrderEvent> deserializer() {
        return orderEventDeserializer;
    }
}
