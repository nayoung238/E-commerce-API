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
        ObjectMapper mapper = new ObjectMapper();
        try {
            Map<Object, Object> map = mapper.readValue(kafkaMessage, new TypeReference<Map<Object, Object>>() {
            });
            Object[] orderItems = mapper.convertValue(map.get("orderItems"), Object[].class);

            List<OrderItemRequest> orderItemRequests = new ArrayList<>();
            for (Object orderItem : orderItems)
                orderItemRequests.add(OrderItemRequest.fromKafkaMessage(orderItem));

            ItemStockUpdateRequest request = ItemStockUpdateRequest.fromKafkaMessage(map, orderItemRequests);
            ItemStockUpdateResponse response = orderItemService.updateItemsStock(request);
            kafkaProducer.send("update-order-status-topic", response);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
