package com.nayoung.orderservice.domain;

import com.nayoung.orderservice.exception.ExceptionCode;
import com.nayoung.orderservice.exception.OrderStatusException;
import com.nayoung.orderservice.messagequeue.client.ItemUpdateStatus;

import java.util.Objects;

public enum OrderStatus {

    SUCCEEDED, FAILED, WAITING, CANCELED, OUT_OF_STOCK;

    public static OrderStatus getOrderStatus(ItemUpdateStatus itemUpdateStatus) {
        if(Objects.equals(ItemUpdateStatus.SUCCEEDED, itemUpdateStatus)) return SUCCEEDED;
        if(Objects.equals(ItemUpdateStatus.FAILED, itemUpdateStatus)) return FAILED;
        if(Objects.equals(ItemUpdateStatus.CANCELED, itemUpdateStatus)) return CANCELED;
        if(Objects.equals(ItemUpdateStatus.OUT_OF_STOCK, itemUpdateStatus)) return OUT_OF_STOCK;
        throw new OrderStatusException(ExceptionCode.NO_MATCHING_ORDER_STATUS);
     }
}
