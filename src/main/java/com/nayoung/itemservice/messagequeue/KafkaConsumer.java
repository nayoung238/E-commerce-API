package com.nayoung.itemservice.messagequeue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nayoung.itemservice.domain.item.RedissonItemService;
import com.nayoung.itemservice.web.dto.ItemStockUpdateDto;
import lombok.Builder;
import lombok.Getter;
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
    private final RedissonItemService redissonItemService;

    @KafkaListener(topics = "e-commerce.order.order-details")
    public void updateStock(String kafkaMessage)  {
        OrderDetails result = redissonItemService.updateItemStockByOrderDetails(Objects.requireNonNull(getOrderDetails(kafkaMessage)));
        kafkaProducer.send("update-order-status-topic", result);
    }

    private OrderDetails getOrderDetails(String message) {
        Map<Object, Object> map;
        ObjectMapper mapper = new ObjectMapper();

        try {
            map = mapper.readValue(message, new TypeReference<Map<Object, Object>>() {});
            Object[] orderItems = mapper.convertValue(map.get("orderItemDtos"), Object[].class);
            List<ItemStockUpdateDto> itemStockUpdateDtos = new ArrayList<>();
            for (Object orderItem : orderItems) {
                itemStockUpdateDtos.add(ItemStockUpdateDto.fromKafkaMessage(
                        Long.parseLong(String.valueOf(map.get("orderId"))),
                        Long.parseLong(String.valueOf(map.get("customerAccountId"))),
                        orderItem));
            }

            return OrderDetails.builder()
                    .orderId(Long.parseLong(String.valueOf(map.get("orderId"))))
                    .customerAccountId(Long.parseLong(String.valueOf(map.get("customerAccountId"))))
                    .createdAt(String.valueOf(map.get("createdAt")))
                    .itemStockUpdateDtos(itemStockUpdateDtos)
                    .build();

        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Builder @Getter
    public static class OrderDetails {
        private Long orderId;
        private Long customerAccountId;
        private String createdAt;
        private List<ItemStockUpdateDto> itemStockUpdateDtos;
    }
}
