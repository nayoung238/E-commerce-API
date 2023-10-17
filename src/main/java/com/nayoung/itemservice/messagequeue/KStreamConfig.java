package com.nayoung.itemservice.messagequeue;

import com.nayoung.itemservice.domain.item.ItemStockService;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Properties;

@Configuration
@RequiredArgsConstructor
public class KStreamConfig {

    private final String APPLICATION_ID_CONFIG = "item_stock_application";
    private final ItemStockService itemStockService;

    @Bean
    public Properties kstramProperties() {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, APPLICATION_ID_CONFIG);
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.Long().getClass());
        return props;
    }

    @Bean
    public void sumQuantityOfItems() {
        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, Long> stream = builder.stream(KafkaProducerConfig.ITEM_LOG_TOPIC);

        KTable<Windowed<String>, Long> aggregatedStock = stream.groupByKey()
                .windowedBy(TimeWindows.ofSizeAndGrace(Duration.ofSeconds(5), Duration.ofDays(1)))
                .reduce(Long::sum);

        aggregatedStock.toStream().foreach((key, value) -> {
            itemStockService.updateStockOnDB(Long.valueOf(String.valueOf(key.key())), value);
        });

        KafkaStreams kafkaStreams = new KafkaStreams(builder.build(), kstramProperties());
        kafkaStreams.start();
    }
}
