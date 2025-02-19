package com.ecommerce.accountservice.kafka.service.consumer;

import com.ecommerce.accountservice.account.service.CouponService;
import com.ecommerce.accountservice.kafka.config.TopicConfig;
import com.ecommerce.accountservice.kafka.dto.CouponIssuanceResultKafkaEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service @Slf4j
@RequiredArgsConstructor
public class KafkaConsumerService {

    private final CouponService couponService;

    @KafkaListener(topics = TopicConfig.COUPON_ISSUANCE_RESULT_TOPIC)
    public void addCoupon(ConsumerRecord<String, CouponIssuanceResultKafkaEvent> record) {
        log.info("Event consumed successfully -> Topic: {}, AccountId: {}, CouponId: {}",
                record.topic(),
                record.value().getAccountId(),
                record.value().getCouponId());

        couponService.addCoupon(record.value());
    }
}
