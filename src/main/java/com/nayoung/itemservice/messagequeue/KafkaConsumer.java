package com.nayoung.itemservice.messagequeue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nayoung.itemservice.domain.ItemService;
import com.nayoung.itemservice.exception.StockException;
import com.nayoung.itemservice.web.dto.ItemStockUpdateResult;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class KafkaConsumer {

    private final ItemService itemService;
    private final KafkaProducer kafkaProducer;

    @KafkaListener(topics = "update-stock-topic")
    public void updateStock(String kafkaMessage) {
        Map<Object, Object> map = new HashMap<>();
        ObjectMapper mapper = new ObjectMapper();
        try {
            map = mapper.readValue(kafkaMessage, new TypeReference<Map<Object, Object>>() {});
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        ItemStockUpdateResult itemStockUpdateResult = ItemStockUpdateResult.fromKafkaMessage(map);
        try {
            Long itemId = Long.parseLong(String.valueOf(map.get("itemId")));
            Long quantity = Long.parseLong(String.valueOf(map.get("quantity")));
            itemService.decreaseStock(itemId, quantity);
            itemStockUpdateResult.setAvailable(true);
        } catch (StockException e) {
            itemStockUpdateResult.setAvailable(false);
        }
        kafkaProducer.send("update-order-status-topic", itemStockUpdateResult);
    }
}
