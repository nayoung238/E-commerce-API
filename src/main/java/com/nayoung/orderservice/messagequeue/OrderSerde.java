package com.nayoung.orderservice.messagequeue;

import com.nayoung.orderservice.web.dto.OrderDto;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

public class OrderSerde implements Serde<OrderDto> {

    private final OrderDtoSerializer orderDtoSerializer = new OrderDtoSerializer();
    private final OrderDtoDeserializer orderDtoDeserializer = new OrderDtoDeserializer();

    public OrderSerde() {}

    @Override
    public void close() {
        Serde.super.close();
        orderDtoSerializer.close();
        orderDtoDeserializer.close();
    }

    @Override
    public Serializer<OrderDto> serializer() {
        return orderDtoSerializer;
    }

    @Override
    public Deserializer<OrderDto> deserializer() {
        return orderDtoDeserializer;
    }
}
