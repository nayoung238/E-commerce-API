package com.ecommerce.orderservice.domain.order;

public enum OrderProcessingStatus {

    PROCESSING,

    SUCCESSFUL,

    FAILED,
    CANCELED,
    OUT_OF_STOCK,

    NOT_EXIST,
    BAD_REQUEST,
    ITEM_NOT_FOUND,

    SERVER_ERROR;
}
