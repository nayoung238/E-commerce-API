package com.nayoung.itemservice.messagequeue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nayoung.itemservice.domain.Item;
import com.nayoung.itemservice.domain.ItemRepository;
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

    private final ItemRepository itemRepository;
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

        Item item = itemRepository.findById(Long.parseLong(String.valueOf(map.get("itemId")))).orElseThrow();
        boolean isAvailableStockUpdate;
        try {
            item.updateStock(Long.parseLong(String.valueOf(map.get("quantity"))));
            itemRepository.save(item);
            isAvailableStockUpdate = true;
        } catch (StockException e) {
            isAvailableStockUpdate = false;
        }
        kafkaProducer.send("update-order-status-topic",
                ItemStockUpdateResult.fromKafkaMessageAndItemEntity(isAvailableStockUpdate, map, item));
    }
}
