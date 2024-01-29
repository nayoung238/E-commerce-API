package com.nayoung.itemservice.kafka.dto;

import com.nayoung.itemservice.exception.ExceptionCode;
import com.nayoung.itemservice.exception.OrderException;

import java.util.Objects;

public enum OrderItemStatus {

    SUCCEEDED, FAILED, WAITING, CANCELED, OUT_OF_STOCK;

    public static OrderItemStatus getOrderItemStatus(String orderItemStatus) {
        if(Objects.equals(orderItemStatus, "SUCCEEDED")) return SUCCEEDED;
        if(Objects.equals(orderItemStatus, "FAILED")) return FAILED;
        throw new OrderException(ExceptionCode.NO_MATCHING_ORDER_ITEM_STATUS);
    }
}
