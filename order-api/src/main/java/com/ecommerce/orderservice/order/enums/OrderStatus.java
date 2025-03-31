package com.ecommerce.orderservice.order.enums;

public enum OrderStatus {

    PROCESSING,

    SUCCESSFUL,

    FAILED,
    CANCELED,
    OUT_OF_STOCK,

    NOT_EXIST,
    BAD_REQUEST,
    ITEM_NOT_FOUND,

    SERVER_ERROR,

    CREATION,
    UPDATE;
}
