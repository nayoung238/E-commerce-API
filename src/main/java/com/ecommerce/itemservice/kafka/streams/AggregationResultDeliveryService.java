package com.ecommerce.itemservice.kafka.streams;

import com.ecommerce.itemservice.domain.item.service.ItemStockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Windowed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AggregationResultDeliveryService {

    private final ItemStockService itemStockService;

    @Autowired
    public void updateStock(KStream<Windowed<String>, Long> windowedLongKStream) {
        windowedLongKStream.foreach((key, value) -> {
            Long itemId = Long.valueOf(key.key());
            log.info("Aggregation results using Kafka streams -> itemId " + itemId + ", quantity " + value);
            itemStockService.updateStockWithPessimisticLock(itemId, value);
        });
    }
}