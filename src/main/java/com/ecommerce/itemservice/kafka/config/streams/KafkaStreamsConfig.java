package com.ecommerce.itemservice.kafka.config.streams;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.WindowStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration;
import org.springframework.kafka.config.KafkaStreamsConfiguration;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.ecommerce.itemservice.kafka.config.producer.KafkaProducerConfig.ITEM_UPDATE_LOG_TOPIC;

@Configuration
@EnableKafkaStreams
@Slf4j
public class KafkaStreamsConfig {

    @Value("${spring.kafka.streams.application-id}")
    private String STREAM_APPLICATION_ID;

    @Value("${spring.kafka.bootstrap-servers}")
    private String BOOTSTRAP_SERVER;

    @Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
    public KafkaStreamsConfiguration kafkaStreamsConfiguration() {
        Map<String, Object> props = new HashMap<>();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, STREAM_APPLICATION_ID);
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVER);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.Long().getClass());
        return new KafkaStreamsConfiguration(props);
    }


    @Bean
    public KStream<Windowed<String>, Long> addUpQuantityOfItems(KafkaStreamsConfiguration kafkaStreamsConfiguration,
                                                                StreamsBuilder streamsBuilder) {
        AdminClient adminClient = AdminClient.create(kafkaStreamsConfiguration.asProperties());
        adminClient.createTopics(Collections.singleton(
                new NewTopic(ITEM_UPDATE_LOG_TOPIC, 1, (short)1)));

        KStream<String, Long> stream = streamsBuilder.stream(ITEM_UPDATE_LOG_TOPIC);

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
                .peek(((key, value) ->
                        log.info("Kafka Windowed: " + key.window().startTime()+ " - " + key.window().endTime())));
    }
}