package com.ecommerce.accountservice.openfeign.client;

public enum OrderItemStatus {
    SUCCEEDED,
    FAILED,
    WAITING,
    CANCELED,
    OUT_OF_STOCK,
    SERVER_ERROR,
    BAD_REQUEST
}