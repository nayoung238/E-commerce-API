package com.nayoung.itemservice.web.dto;

import com.nayoung.itemservice.domain.Item;
import lombok.Data;
import java.util.Map;

@Data
public class ItemStockUpdateResult {
    boolean isAvailable;
    long orderId;
    long quantity;
    long itemId;
    long stock;

    private ItemStockUpdateResult(boolean status, Map<Object, Object> kafkaMessage, Item item) {
        this.isAvailable = status;
        this.orderId = Long.parseLong(String.valueOf(kafkaMessage.get("orderId")));
        this.quantity = Long.parseLong(String.valueOf(kafkaMessage.get("quantity")));
        this.itemId = item.getId();
        this.stock = item.getStock();
    }

    public static ItemStockUpdateResult fromKafkaMessageAndItemEntity(boolean status, Map<Object, Object> kafkaMessage, Item item) {
        return new ItemStockUpdateResult(status, kafkaMessage, item);
    }
}
