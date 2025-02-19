package com.ecommerce.couponservice.internalevent;

import com.ecommerce.couponservice.coupon.entity.Coupon;
import com.ecommerce.couponservice.coupon.repository.CouponRepository;
import com.ecommerce.couponservice.common.exception.ExceptionCode;
import com.ecommerce.couponservice.internalevent.couponissuanceresult.CouponIssuanceResultId;
import com.ecommerce.couponservice.internalevent.couponissuanceresult.CouponIssuanceResultInternalEvent;
import com.ecommerce.couponservice.internalevent.service.InternalEventService;
import com.ecommerce.couponservice.kafka.config.TopicConfig;
import com.ecommerce.couponservice.kafka.dto.CouponIssuanceResultKafkaEvent;
import com.ecommerce.couponservice.kafka.service.producer.KafkaProducerService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@RequiredArgsConstructor
@Component
public class InternalEventListener {

    private final InternalEventService internalEventService;
    private final KafkaProducerService kafkaProducerService;
    private final CouponRepository couponRepository;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void insertInternalEvent(CouponIssuanceResultInternalEvent event) {
        internalEventService.saveInternalEvent(event);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendKafkaEvent(CouponIssuanceResultInternalEvent event) {
        CouponIssuanceResultId id = event.getId();
        Coupon coupon = couponRepository.findById(id.getCouponId()).
                orElseThrow(() -> new EntityNotFoundException(ExceptionCode.NOT_FOUND_COUPON.getMessage()));

        CouponIssuanceResultKafkaEvent issuanceResult = CouponIssuanceResultKafkaEvent.of(id, coupon.getName());
        kafkaProducerService.send(TopicConfig.COUPON_ISSUANCE_RESULT_TOPIC, null, issuanceResult);
    }
}
