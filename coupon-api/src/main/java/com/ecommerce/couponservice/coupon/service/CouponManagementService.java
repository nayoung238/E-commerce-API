package com.ecommerce.couponservice.coupon.service;

import com.ecommerce.couponservice.coupon.entity.Coupon;
import com.ecommerce.couponservice.coupon.dto.CouponDto;
import com.ecommerce.couponservice.coupon.dto.CouponIssuanceResultDto;
import com.ecommerce.couponservice.coupon.dto.CouponRegisterRequestDto;
import com.ecommerce.couponservice.coupon.repository.CouponRepository;
import com.ecommerce.couponservice.common.exception.ExceptionCode;
import com.ecommerce.couponservice.internalevent.service.InternalEventService;
import com.ecommerce.couponservice.redis.manager.CouponIssuanceStatus;
import com.ecommerce.couponservice.redis.manager.CouponStockRedisManager;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CouponManagementService {

    private final CouponRepository couponRepository;
    private final CouponStockRedisManager couponStockRedisManager;
    private final InternalEventService internalEventService;

    @Transactional
    public CouponDto register(CouponRegisterRequestDto couponRegisterRequestDto) {
        if(couponRepository.existsByName(couponRegisterRequestDto.getName()))
            throw new IllegalArgumentException(ExceptionCode.DUPLICATE_COUPON_NAME.getMessage());

        Coupon coupon = Coupon.of(couponRegisterRequestDto);
        couponRepository.save(coupon);
        return CouponDto.of(coupon);
    }

    public void loadCouponStockToRedis(Long couponId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new EntityNotFoundException(ExceptionCode.NOT_FOUND_COUPON.getMessage()));

        couponStockRedisManager.registerCoupon(coupon.getId(), coupon.getName(), coupon.getQuantity());
    }

    public CouponDto findCouponById(Long couponId) {
        return CouponDto.of(couponRepository.findById(couponId)
                .orElseThrow(() -> new EntityNotFoundException(ExceptionCode.NOT_FOUND_COUPON.getMessage())));
    }

    @Transactional
    public CouponIssuanceStatus issueCouponInDatabase(Long couponId) {
        Coupon coupon = couponRepository.findByIdWithPessimisticLock(couponId)
                .orElseThrow(() -> new EntityNotFoundException(ExceptionCode.NOT_FOUND_COUPON.getMessage()));

        return coupon.decrementQuantity();
    }

    @Transactional
    public CouponIssuanceResultDto issueCouponInDatabase(Long couponId, Long accountId) {
        Coupon coupon = couponRepository.findByIdWithPessimisticLock(couponId)
                .orElseThrow(() -> new EntityNotFoundException(ExceptionCode.NOT_FOUND_COUPON.getMessage()));

        CouponIssuanceStatus status = coupon.decrementQuantity();
        return CouponIssuanceResultDto.of(couponId, accountId, status);
    }
}
