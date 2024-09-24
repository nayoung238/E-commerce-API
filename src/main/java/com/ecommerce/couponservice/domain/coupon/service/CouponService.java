package com.ecommerce.couponservice.domain.coupon.service;

import com.ecommerce.couponservice.domain.coupon.Coupon;
import com.ecommerce.couponservice.domain.coupon.dto.CouponDto;
import com.ecommerce.couponservice.domain.coupon.dto.CouponRegisterRequestDto;
import com.ecommerce.couponservice.domain.coupon.repo.CouponRepository;
import com.ecommerce.couponservice.exception.ExceptionCode;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;

    @Transactional
    public CouponDto register(CouponRegisterRequestDto couponRegisterRequestDto) {
        if(couponRepository.existsByName(couponRegisterRequestDto.getName()))
            throw new IllegalArgumentException(ExceptionCode.DUPLICATE_COUPON_NAME.getMessage());

        Coupon coupon = Coupon.of(couponRegisterRequestDto);
        couponRepository.save(coupon);
        return CouponDto.of(coupon);
    }

    public CouponDto findCouponById(Long id) {
        return CouponDto.of(couponRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ExceptionCode.NOT_FOUND_COUPON.getMessage())));
    }
}
