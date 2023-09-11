package com.nayoung.itemservice.messagequeue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nayoung.itemservice.domain.item.RedissonItemService;
import com.nayoung.itemservice.domain.item.log.ItemUpdateLog;
import com.nayoung.itemservice.domain.item.log.ItemUpdateStatus;
import com.nayoung.itemservice.web.dto.ItemUpdateLogDto;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.*;

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
            Long orderId = Long.parseLong(String.valueOf(map.get("orderId")));
            Long customerAccountId = Long.parseLong(String.valueOf(map.get("customerAccountId")));
            String createdAt = String.valueOf(map.get("createdAt"));

            Object[] orderItems = mapper.convertValue(map.get("orderItemDtos"), Object[].class);
            List<ItemStockUpdateDetails> itemStockUpdateRequestInfos = new ArrayList<>();
            for (Object orderItem : orderItems)
                itemStockUpdateRequestInfos.add(ItemStockUpdateDetails.fromKafkaMessage(orderItem));

            return OrderDetails.builder()
                    .orderId(orderId)
                    .customerAccountId(customerAccountId)
                    .createdAt(createdAt)
                    .itemStockUpdateDetailsList(itemStockUpdateRequestInfos)
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
        private List<ItemStockUpdateDetails> itemStockUpdateDetailsList;
    }

    @Builder @Getter
    public static class ItemStockUpdateDetails {
        private ItemUpdateStatus itemUpdateStatus;
        private Long itemId;
        private Long quantity;

        private static ItemStockUpdateDetails fromKafkaMessage(Object orderItem) {
            ObjectMapper objectMapper = new ObjectMapper();
            Map map = objectMapper.convertValue(orderItem, Map.class);
            return ItemStockUpdateDetails.builder()
                    .itemId(Long.parseLong(String.valueOf(map.get("itemId"))))
                    .quantity(Long.parseLong(String.valueOf(map.get("quantity"))))
                    .build();
        }

        public static ItemStockUpdateDetails fromUndoItemUpdateLog(ItemUpdateLog itemUpdateLog) {
            return ItemStockUpdateDetails.builder()
                    .itemUpdateStatus(ItemUpdateStatus.CANCELED)
                    .itemId(itemUpdateLog.getItemId())
                    .quantity(-itemUpdateLog.getQuantity())
                    .build();
        }

        public static ItemStockUpdateDetails fromItemUpdateLogDto(ItemUpdateLogDto itemUpdateLogDto) {
            return ItemStockUpdateDetails.builder()
                    .itemUpdateStatus(itemUpdateLogDto.getItemUpdateStatus())
                    .itemId(itemUpdateLogDto.getItemId())
                    .quantity(itemUpdateLogDto.getQuantity())
                    .build();
        }

        public void setItemUpdateStatus(ItemUpdateStatus itemUpdateStatus) {
            this.itemUpdateStatus = itemUpdateStatus;
        }
    }
}
