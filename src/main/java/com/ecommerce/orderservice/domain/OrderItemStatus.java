package com.ecommerce.orderservice.domain;

public enum OrderItemStatus {

    SUCCEEDED,
    FAILED,
    WAITING,
    CANCELED,
    OUT_OF_STOCK,
    NOT_EXIST,
    SERVER_ERROR,
    BAD_REQUEST
}
