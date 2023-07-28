package com.nayoung.itemservice.messagequeue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nayoung.itemservice.domain.item.OrderItemService;
import com.nayoung.itemservice.web.dto.ItemStockUpdateRequest;
import com.nayoung.itemservice.web.dto.ItemStockUpdateResponse;
import com.nayoung.itemservice.web.dto.OrderItemRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumer {

    private final OrderItemService orderItemService;
    private final KafkaProducer kafkaProducer;

    @KafkaListener(topics = "update-stock-topic")
    public void updateStock(String kafkaMessage)  {
        ItemStockUpdateRequest request = getItemStockUpdateRequest(kafkaMessage);
        if(request != null) {
            ItemStockUpdateResponse response = orderItemService.updateItemsStock(request);
            kafkaProducer.send("update-order-status-topic", response);
        }
    }

    private ItemStockUpdateRequest getItemStockUpdateRequest(String message) {
        List<OrderItemRequest> orderItemRequests = new ArrayList<>();
        Map<Object, Object> map;
        ObjectMapper mapper = new ObjectMapper();

        try {
            map = mapper.readValue(message, new TypeReference<Map<Object, Object>>() {});
            Object[] orderItems = mapper.convertValue(map.get("orderItems"), Object[].class);

            for (Object orderItem : orderItems)
                orderItemRequests.add(OrderItemRequest.fromKafkaMessage(orderItem));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
        return ItemStockUpdateRequest.fromKafkaMessage(map, orderItemRequests);
    }
}
