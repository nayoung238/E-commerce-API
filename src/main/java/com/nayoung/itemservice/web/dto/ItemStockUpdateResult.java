package com.nayoung.itemservice.web.dto;

import lombok.Data;
import java.util.Map;

@Data
public class ItemStockUpdateResult {
    boolean isAvailable;
    long itemId;
    long orderId;
    long requestedQuantity;

    private ItemStockUpdateResult(Map<Object, Object> kafkaMessage) {
        this.orderId = Long.parseLong(String.valueOf(kafkaMessage.get("orderId")));
        this.requestedQuantity = Long.parseLong(String.valueOf(kafkaMessage.get("quantity")));
        this.itemId = Long.parseLong(String.valueOf(kafkaMessage.get("itemId")));
    }

    public static ItemStockUpdateResult fromKafkaMessage(Map<Object, Object> kafkaMessage) {
        return new ItemStockUpdateResult(kafkaMessage);
    }
}
