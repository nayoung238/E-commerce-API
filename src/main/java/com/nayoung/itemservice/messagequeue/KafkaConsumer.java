package com.nayoung.itemservice.messagequeue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nayoung.itemservice.domain.item.RedissonItemService;
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
    private final RedissonItemService redissonItemService;

    @KafkaListener(topics = "update-stock-topic")
    public void updateStock(String kafkaMessage)  {
        List<ItemStockToUpdateDto> itemStockToUpdateDtos = getItemStockUpdateRequest(kafkaMessage);

        assert itemStockToUpdateDtos != null;
        List<ItemStockToUpdateDto> result = itemStockToUpdateDtos.stream()
                .map(redissonItemService::decreaseStock)
                .collect(Collectors.toList());

        boolean isExistOutOfStockItem = result.stream()
                .anyMatch(r -> Objects.equals(OrderStatus.OUT_OF_STOCK, r.getOrderStatus()));

        if(isExistOutOfStockItem) {
            redissonItemService.undo(result.get(0).getOrderId());
            for(ItemStockToUpdateDto itemStockToUpdateDto : result)
                itemStockToUpdateDto.setOrderStatus(OrderStatus.FAILED);
        }
        kafkaProducer.send("update-order-status-topic", result);
    }

    private List<ItemStockToUpdateDto> getItemStockUpdateRequest(String message) {
        List<ItemStockToUpdateDto> itemStockToUpdateDtos = new ArrayList<>();
        Map<Object, Object> map;
        ObjectMapper mapper = new ObjectMapper();

        try {
            map = mapper.readValue(message, new TypeReference<Map<Object, Object>>() {});
            Object[] orderItems = mapper.convertValue(map.get("orderItemDtos"), Object[].class);

            for (Object orderItem : orderItems)
                itemStockToUpdateDtos.add(ItemStockToUpdateDto.fromKafkaMessage(orderItem));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
        return itemStockToUpdateDtos;
    }
}
