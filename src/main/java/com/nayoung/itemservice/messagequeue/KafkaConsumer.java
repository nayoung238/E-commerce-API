package com.nayoung.itemservice.messagequeue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nayoung.itemservice.domain.item.RedissonItemService;
import com.nayoung.itemservice.domain.item.log.ItemUpdateStatus;
import com.nayoung.itemservice.web.dto.ItemStockUpdateDto;
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
        List<ItemStockUpdateDto> itemStockUpdateDtos = getItemStockUpdateRequest(kafkaMessage);

        assert itemStockUpdateDtos != null;
        List<ItemStockUpdateDto> result = itemStockUpdateDtos.stream()
                .map(redissonItemService::updateStock)
                .collect(Collectors.toList());

        boolean isExistOutOfStockItem = result.stream()
                .anyMatch(r -> Objects.equals(ItemUpdateStatus.OUT_OF_STOCK, r.getItemUpdateStatus()));

        if(isExistOutOfStockItem) {
            redissonItemService.undo(result.get(0).getOrderId());
            for(ItemStockUpdateDto itemStockUpdateDto : result)
                itemStockUpdateDto.setItemUpdateStatus(ItemUpdateStatus.FAILED);
        }
        kafkaProducer.send("update-order-status-topic", result);
    }

    private List<ItemStockUpdateDto> getItemStockUpdateRequest(String message) {
        List<ItemStockUpdateDto> itemStockUpdateDtos = new ArrayList<>();
        Map<Object, Object> map;
        ObjectMapper mapper = new ObjectMapper();

        try {
            map = mapper.readValue(message, new TypeReference<Map<Object, Object>>() {});
            Object[] orderItems = mapper.convertValue(map.get("orderItemDtos"), Object[].class);

            for (Object orderItem : orderItems) {
                itemStockUpdateDtos.add(ItemStockUpdateDto.fromKafkaMessage(
                        Long.parseLong(String.valueOf(map.get("orderId"))),
                        Long.parseLong(String.valueOf(map.get("customerAccountId"))),
                        orderItem));
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
        return itemStockUpdateDtos;
    }
}
