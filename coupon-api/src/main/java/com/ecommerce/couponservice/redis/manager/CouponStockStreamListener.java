package com.ecommerce.couponservice.redis.manager;

import com.ecommerce.couponservice.coupon.service.CouponManagementService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RequiredArgsConstructor
@Component
@Slf4j
public class CouponStockStreamListener implements StreamListener<String, MapRecord<String, String, String>> {

    private final StringRedisTemplate stringRedisTemplate;
    private StreamMessageListenerContainer<String, MapRecord<String, String, String>> container;
    private final CouponManagementService couponManagementService;

    @Value("${redis.streams.name}")
    private String streamsKey;

    @Value("${redis.streams.consumer.group}")
    private String consumerGroup;

    @Value("${redis.streams.consumer.name}")
    private String consumerName;

    @PostConstruct
    public void startListening() {
        createConsumerGroup(streamsKey, consumerGroup);

        container = StreamMessageListenerContainer.create(
                Objects.requireNonNull(stringRedisTemplate.getConnectionFactory()),
                StreamMessageListenerContainer.StreamMessageListenerContainerOptions
                        .builder()
                        .pollTimeout(Duration.ofSeconds(1))
                        .build()
        );

        container.receive(
                Consumer.from(consumerGroup, consumerName),
                StreamOffset.create(streamsKey, ReadOffset.lastConsumed()), //latest(streamsKey),
                this::onMessage
        );

        container.start();
        log.info("Started listening to coupon_stock_streams_key");
    }

    @Override
    public void onMessage(MapRecord<String, String, String> message) {
        try {
            Long couponId = Long.parseLong(message.getValue().get("couponId"));
            CouponIssuanceStatus status = couponManagementService.issueCouponInDatabase(couponId);
            // TODO: if(status == CouponIssuanceStatus.OUT_OF_STOCK) {...}
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    @PreDestroy
    public void stopListening() {
        if(container != null) {
            container.stop();
            log.info("Stopped listening to coupon_stock_streams_key");
        }
    }

    private void createConsumerGroup(String streamKey, String groupName) {
        try {
            if(Boolean.FALSE.equals(stringRedisTemplate.hasKey(streamKey))) {
                Map<String , String> content = new HashMap<>();
                content.put("coupon_id", "-1");
                content.put("new_stock", "0");
                stringRedisTemplate.opsForStream().add(streamKey, content);
            }

            if(!isExistConsumerGroup(streamKey, groupName)) {
                stringRedisTemplate
                        .opsForStream()
                        .createGroup(streamKey, ReadOffset.from("0"), groupName);

                log.info("Consumer group created: {}", groupName);
            }
        } catch (Exception e) {
            log.error("Error while creating consumer group", e);
        }
    }

    private boolean isExistConsumerGroup(String streamKey, String groupName) {
        return stringRedisTemplate
                .opsForStream()
                .groups(streamKey)
                .stream()
                .anyMatch(group -> group.groupName().equals(groupName));
    }
}