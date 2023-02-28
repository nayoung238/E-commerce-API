package com.nayoung.itemservice.messagequeue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nayoung.itemservice.domain.Item;
import com.nayoung.itemservice.domain.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class KafkaConsumer {

    private final ItemRepository itemRepository;

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
        item.updateStock(Long.parseLong(String.valueOf(map.get("quantity"))));
        itemRepository.save(item);
    }
}
