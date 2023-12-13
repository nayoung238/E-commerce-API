package com.nayoung.itemservice.messagequeue;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration;
import org.springframework.kafka.config.KafkaStreamsConfiguration;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafkaStreams
public class KafkaStreamsConfig {

    private final String APPLICATION_ID_CONFIG = "item_stock_application";

    @Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
    public KafkaStreamsConfiguration kafkaStreamsConfiguration() {
        Map<String, Object> props = new HashMap<>();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, APPLICATION_ID_CONFIG);
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.Long().getClass());
        return new KafkaStreamsConfiguration(props);
    }

    @Bean
    public KTable<Windowed<String>, Long> addUpQuantityOfItems(StreamsBuilder streamsBuilder) {
        KStream<String, Long> stream = streamsBuilder.stream(KafkaProducerConfig.ITEM_UPDATE_LOG_TOPIC);

        return stream.groupByKey()
                .windowedBy(TimeWindows.ofSizeAndGrace(Duration.ofSeconds(3), Duration.ofDays(1)))
                .reduce(Long::sum);
    }
}