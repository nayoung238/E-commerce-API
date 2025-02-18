package com.ecommerce.itemservice.kafka.dto;

import com.ecommerce.itemservice.domain.item.ItemProcessingStatus;
import lombok.extern.slf4j.Slf4j;

import java.util.Locale;

@Slf4j
public enum OrderProcessingStatus {

    PROCESSING,

    SUCCESSFUL,

    FAILED,
    CANCELED,
    OUT_OF_STOCK,

    NOT_EXIST,
    BAD_REQUEST,
    ITEM_NOT_FOUND;

    public static OrderProcessingStatus getStatus(String status) {
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

    public static OrderProcessingStatus getStatus(ItemProcessingStatus itemProcessingStatus) {
        assert itemProcessingStatus != null;
        return switch (itemProcessingStatus) {
            case SUCCESSFUL_CONSUMPTION -> SUCCESSFUL;
            case FAILED_CONSUMPTION -> FAILED;
            case OUT_OF_STOCK -> OUT_OF_STOCK;
            case ITEM_NOT_FOUND -> ITEM_NOT_FOUND;
            case SUCCESSFUL_PRODUCTION -> CANCELED;
            default -> throw new IllegalArgumentException("Unknown item processing status: " + itemProcessingStatus);
        };
    }
}
