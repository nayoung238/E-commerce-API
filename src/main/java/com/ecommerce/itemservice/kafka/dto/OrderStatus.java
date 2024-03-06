package com.ecommerce.itemservice.kafka.dto;

import lombok.extern.slf4j.Slf4j;

import java.util.Locale;

@Slf4j
public enum OrderStatus {

    SUCCEEDED,
    FAILED,
    WAITING,
    CANCELED,
    OUT_OF_STOCK,
    NOT_EXIST,
    BAD_REQUEST;

    public static OrderStatus getStatus(String status) {
        try {
            if(status != null)
                return valueOf(status.toUpperCase(Locale.ROOT));
            else
                return BAD_REQUEST;
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
            return BAD_REQUEST;
        }
    }
}
