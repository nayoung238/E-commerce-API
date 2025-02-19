package com.ecommerce.couponservice.internalevent.service;

import com.ecommerce.couponservice.common.exception.ExceptionCode;
import com.ecommerce.couponservice.common.exception.InternalEventException;
import com.ecommerce.couponservice.internalevent.InternalEventStatus;
import com.ecommerce.couponservice.internalevent.couponissuanceresult.CouponIssuanceResultId;
import com.ecommerce.couponservice.internalevent.couponissuanceresult.CouponIssuanceResultInternalEvent;
import com.ecommerce.couponservice.internalevent.couponissuanceresult.repo.CouponIssuanceResultInternalEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InternalEventService {

    private final ApplicationEventPublisher eventPublisher;
    private final CouponIssuanceResultInternalEventRepository couponIssuanceResultInternalEventRepository;

    @Transactional
    public void publishInternalEvent(CouponIssuanceResultInternalEvent event) {
        eventPublisher.publishEvent(event);
    }

    @Transactional
    public void saveInternalEvent(CouponIssuanceResultInternalEvent event) {
        couponIssuanceResultInternalEventRepository.save(event);
    }

    @Transactional
    public void updatePublicationStatus(long couponId, long accountId, InternalEventStatus status) {
        CouponIssuanceResultId id = CouponIssuanceResultId.of(couponId, accountId);
        CouponIssuanceResultInternalEvent event = couponIssuanceResultInternalEventRepository.findById(id)
                .orElseThrow(() -> new InternalEventException(ExceptionCode.NOT_FOUND_INTERNAL_EVENT));
        event.updatePublicationStatus(status);
    }
}
