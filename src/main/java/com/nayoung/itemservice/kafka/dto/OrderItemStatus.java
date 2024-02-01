package com.nayoung.itemservice.kafka.dto;

import lombok.extern.slf4j.Slf4j;

import java.util.Locale;

@Slf4j
public enum OrderItemStatus {

    SUCCEEDED,
    FAILED,
    WAITING,
    CANCELED,
    OUT_OF_STOCK,
    NOT_EXIST,
    BAD_REQUEST;

    public static OrderItemStatus getOrderItemStatus(String orderItemStatus) {
        try {
            return valueOf(orderItemStatus.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
            return BAD_REQUEST;
        }
    }
}
