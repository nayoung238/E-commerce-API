package com.ecommerce.orderservice.kafka.config.streams;

import com.ecommerce.orderservice.order.enums.OrderProcessingStatus;
import com.ecommerce.orderservice.kafka.config.TopicConfig;
import com.ecommerce.orderservice.kafka.dto.OrderKafkaEvent;
import com.ecommerce.orderservice.kafka.dto.OrderEventSerde;
import com.ecommerce.orderservice.kafka.service.producer.KafkaProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.errors.TopicExistsException;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Produced;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration;
import org.springframework.kafka.config.KafkaStreamsConfiguration;
import org.springframework.kafka.support.serializer.JsonSerde;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@EnableKafkaStreams
@Configuration
@RequiredArgsConstructor
@Slf4j
public class KStreamKTableJoinConfig {

    private final String STATE_DIR = "/tmp/kafka-streams/";
    private final KafkaProducerService kafkaProducerService;

    @Value("${spring.kafka.streams.application-id}")
    private String STREAM_APPLICATION_ID;

    @Value("${spring.kafka.bootstrap-servers}")
    private String BOOTSTRAP_SERVER;

    @Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
    public KafkaStreamsConfiguration kafkaStreamsProperties() {
        Map<String, Object> props = new HashMap<>();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, STREAM_APPLICATION_ID);
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVER);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, OrderEventSerde.class);

        String[] split = UUID.randomUUID().toString().split("-");
        props.put(StreamsConfig.STATE_DIR_CONFIG, STATE_DIR + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm")) + "-" + split[0]);
        return new KafkaStreamsConfiguration(props);
    }

    @Bean
    public AdminClient kafkaStreamsAdminClient(KafkaStreamsConfiguration kafkaStreamsConfiguration) {
        return AdminClient.create(kafkaStreamsConfiguration.asProperties());
    }

    @Bean
    public KTable<String, OrderKafkaEvent> pendingOrders(AdminClient kafkaStreamsAdminClient, StreamsBuilder streamsBuilder) {
        createTopic(kafkaStreamsAdminClient, TopicConfig.REQUESTED_ORDER_STREAMS_ONLY_TOPIC);
        return streamsBuilder.table(TopicConfig.REQUESTED_ORDER_STREAMS_ONLY_TOPIC);
    }

    @Bean
    public KStream<String, OrderKafkaEvent> orderProcessingResults(AdminClient kafkaStreamsAdminClient, StreamsBuilder streamsBuilder) {
        createTopic(kafkaStreamsAdminClient, TopicConfig.ORDER_PROCESSING_RESULT_STREAMS_ONLY_TOPIC);
        return streamsBuilder.stream(TopicConfig.ORDER_PROCESSING_RESULT_STREAMS_ONLY_TOPIC);
    }

    @Bean
    public KStream<String, OrderKafkaEvent> createFinalOrders(KTable<String, OrderKafkaEvent> pendingOrders,
                                                                KStream<String, OrderKafkaEvent> orderProcessingResults) {
        KStream<String, OrderKafkaEvent> finalOrders = orderProcessingResults
                .filter((key, value) -> isValidProcessingStatus(key, value.getOrderProcessingStatus()))
                .join(pendingOrders, (result, pendingOrder) -> setOrderStatus(pendingOrder, result))
                .peek((key, finalOrder) ->
                        log.info("Successfully joined order processing result with pending order: orderEventId={}, finalStatus={}",
                                key,
                                finalOrder.getOrderProcessingStatus())
                );

        finalOrders.to(TopicConfig.FINAL_ORDER_STREAMS_ONLY_TOPIC,
                Produced.with(Serdes.String(), new JsonSerde<>(OrderKafkaEvent.class)));

        return finalOrders;
    }

    private void createTopic(AdminClient adminClient, String topic) {
        try {
            adminClient
                    .createTopics(Collections.singleton(new NewTopic(topic, 1, (short) 1)))
                    .all()
                    .get(10, TimeUnit.SECONDS);
            log.info("Topic {} created successfully", topic);
        } catch (ExecutionException e) {
            if(e.getCause() instanceof TopicExistsException) {
                log.warn("Topic {} already exists", topic);
            }
            else {
                log.error("Topic {} could not be created", topic, e);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Interrupted while creating '{}' topic", topic, e);
            throw new RuntimeException("Topic creation interrupted", e);
        } catch (TimeoutException e) {
            log.error("Timeout while creating '{}' topic", topic, e);
            throw new RuntimeException("Topic creation timed out", e);
        }
    }

    private boolean isValidProcessingStatus(String orderEventId, OrderProcessingStatus orderProcessingStatus) {
        boolean isValidStatus = orderProcessingStatus.equals(OrderProcessingStatus.SUCCESSFUL)
                || orderProcessingStatus.equals(OrderProcessingStatus.FAILED);

        if(!isValidStatus) {
            log.warn("Invalid order processing status: OrderEventId={}, ProcessingStatus={}", orderEventId, orderProcessingStatus);
            kafkaProducerService.setTombstoneRecord(TopicConfig.REQUESTED_ORDER_STREAMS_ONLY_TOPIC, orderEventId);
        }
        return isValidStatus;
    }

    private OrderKafkaEvent setOrderStatus(OrderKafkaEvent orderEvent, OrderKafkaEvent result) {
        orderEvent.updateOrderStatus(result);
        return orderEvent;
    }
}
