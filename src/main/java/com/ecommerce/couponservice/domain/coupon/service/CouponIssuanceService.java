package com.ecommerce.couponservice.domain.coupon.service;

import com.ecommerce.couponservice.domain.coupon.Coupon;
import com.ecommerce.couponservice.domain.coupon.repo.CouponRepository;
import com.ecommerce.couponservice.exception.ExceptionCode;
import com.ecommerce.couponservice.internalevent.couponissuanceresult.CouponIssuanceResultInternalEvent;
import com.ecommerce.couponservice.internalevent.service.InternalEventService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CouponIssuanceService {

    private final CouponRepository couponRepository;
    private final InternalEventService internalEventService;

    @Transactional
    public String issueCoupon(Long couponId, Long accountId) {
        Optional<Coupon> coupon = couponRepository.findByIdWithPessimisticLock(couponId);
        if(coupon.isEmpty()) {
            throw new EntityNotFoundException(ExceptionCode.NOT_FOUND_COUPON.getMessage());
        }
        coupon.get().decrementQuantity();
        internalEventService.publishInternalEvent(CouponIssuanceResultInternalEvent.init(couponId, accountId));
        return "Coupon issued successfully !!";
    }
}
