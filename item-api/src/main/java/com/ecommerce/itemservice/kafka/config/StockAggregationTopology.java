package com.ecommerce.itemservice.kafka.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
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

    private final long WINDOW_SIZE = 2;
    private final long GRACE_PERIOD = 5;

    @Bean
    public KStream<String, Long> itemStockChangesStream(KafkaStreamsConfiguration kafkaStreamsConfiguration,
                                                 StreamsBuilder streamsBuilder) {
        AdminClient adminClient = AdminClient.create(kafkaStreamsConfiguration.asProperties());
        adminClient.createTopics(Collections.singleton(
                new NewTopic(TopicConfig.ITEM_UPDATE_LOG_TOPIC, 1, (short) 1)));

        return streamsBuilder.stream(
                TopicConfig.ITEM_UPDATE_LOG_TOPIC,
                Consumed.with(Serdes.String(), Serdes.Long()));
    }

    @Bean
    public KStream<String, Long> aggregateStockChanges(KStream<String, Long> itemStockChangeStream) {
        KStream<String, Long> aggregatedStockChange = itemStockChangeStream
                .groupByKey()
                .windowedBy(TimeWindows.ofSizeAndGrace(
                        Duration.ofSeconds(WINDOW_SIZE),
                        Duration.ofSeconds(GRACE_PERIOD)))
                .reduce(Long::sum,
                        Materialized
                                .<String, Long, WindowStore<Bytes, byte[]>>as("total-quantity")
                                .withKeySerde(Serdes.String())
                                .withValueSerde(Serdes.Long())
                                .withRetention(Duration.ofMinutes(1))
                        //.withLoggingEnabled(Collections.singletonMap("min.insync.replicas", "1"))
                )
                .suppress(Suppressed.untilWindowCloses(
                        Suppressed.BufferConfig
                                .unbounded()
                                .shutDownWhenFull()))
                .toStream()
                .peek((windowedKey, value) -> {
                    log.info("Aggregation results for Item Id {}: quantity {} in windows [{} - {}]",
                            windowedKey.key(),
                            value,
                            windowedKey.window().startTime(),
                            windowedKey.window().endTime());
                })
                .map((windowedKey, value) -> KeyValue.pair(windowedKey.key(), value));

        aggregatedStockChange
                .filter((key, value) -> value != null && value != 0)
                .peek((key, value) -> {
                    log.info("Windowed aggregation result: Item Id = {}, aggregatedQuantity = {}", key, value);
                })
                .to(TopicConfig.ITEM_STOCK_AGGREGATION_RESULTS_TOPIC,
                        Produced.with(Serdes.String(), Serdes.Long()));

        return aggregatedStockChange;
    }
}
