package com.nayoung.itemservice.messagequeue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nayoung.itemservice.domain.item.log.OrderStatus;
import com.nayoung.itemservice.web.dto.ItemStockToUpdateDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumer {

    private final KafkaProducer kafkaProducer;

    @KafkaListener(topics = "update-stock-topic")
    public void updateStock(String kafkaMessage)  {
        ItemStockToUpdateDto request = getItemStockUpdateRequest(kafkaMessage);
        if(request != null) {
            ItemStockToUpdateDto response = orderItemService.updateItemsStock(request);
            kafkaProducer.send("update-order-status-topic", response);
        }
    }

    private List<ItemStockToUpdateDto> getItemStockUpdateRequest(String message) {
        List<ItemStockToUpdateDto> itemStockToUpdateDtos = new ArrayList<>();
        Map<Object, Object> map;
        ObjectMapper mapper = new ObjectMapper();

        try {
            map = mapper.readValue(message, new TypeReference<Map<Object, Object>>() {});
            Object[] orderItems = mapper.convertValue(map.get("orderItems"), Object[].class);

            for (Object orderItem : orderItems)
                itemStockToUpdateDtos.add(ItemStockToUpdateDto.fromKafkaMessage(orderItem));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
        return itemStockToUpdateDtos;
    }
}
