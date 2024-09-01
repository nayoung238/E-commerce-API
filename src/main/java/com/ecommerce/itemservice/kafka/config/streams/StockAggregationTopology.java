package com.ecommerce.itemservice.kafka.config.streams;

import com.ecommerce.itemservice.domain.item.service.ItemStockService;
import com.ecommerce.itemservice.kafka.config.TopicConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.WindowStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.KafkaStreamsConfiguration;

import java.time.Duration;
import java.util.Collections;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class StockAggregationTopology {

    private final ItemStockService itemStockService;

    @Bean
    public KStream<Windowed<String>, Long> calculateTotalQuantity(KafkaStreamsConfiguration kafkaStreamsConfiguration,
                                                                  StreamsBuilder streamsBuilder) {
        AdminClient adminClient = AdminClient.create(kafkaStreamsConfiguration.asProperties());
        adminClient.createTopics(Collections.singleton(
                new NewTopic(TopicConfig.ITEM_UPDATE_LOG_TOPIC, 1, (short) 1)));

        KStream<String, Long> stream = streamsBuilder.stream(TopicConfig.ITEM_UPDATE_LOG_TOPIC);

        return stream
                .groupByKey()
                .windowedBy(TimeWindows.ofSizeAndGrace(Duration.ofSeconds(5), Duration.ofSeconds(5)))
                .reduce(Long::sum,
                        Materialized
                                .<String, Long, WindowStore<Bytes, byte[]>>as("total-quantity")
                                .withKeySerde(Serdes.String())
                                .withValueSerde(Serdes.Long())
                                .withRetention(Duration.ofMinutes(1))
                        //.withLoggingEnabled(Collections.singletonMap("min.insync.replicas", "1"))
                )
                .suppress(Suppressed.untilWindowCloses(Suppressed.BufferConfig.unbounded().shutDownWhenFull()))
                .toStream()
                .peek((key, value) -> {
                    Long itemId = Long.valueOf(key.key());
                    if(value != 0) {
                        itemStockService.updateStockWithPessimisticLock(itemId, value);
                        log.info("Aggregation results [ {} - {} ] -> itemId={}, quantity={}",
                                key.window().startTime(),
                                key.window().endTime(),
                                itemId,
                                value);
                    }
                });
    }
}
