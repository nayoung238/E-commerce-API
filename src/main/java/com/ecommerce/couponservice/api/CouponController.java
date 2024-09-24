package com.ecommerce.couponservice.api;

import com.ecommerce.couponservice.domain.coupon.dto.CouponDto;
import com.ecommerce.couponservice.domain.coupon.dto.CouponRegisterRequestDto;
import com.ecommerce.couponservice.domain.coupon.service.CouponService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/coupons")
@Validated
public class CouponController {

    private final CouponService couponService;

    @PostMapping
    public ResponseEntity<?> register(@RequestBody @Valid CouponRegisterRequestDto couponRegisterRequestDto) {
        CouponDto response = couponService.register(couponRegisterRequestDto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CouponDto> findCouponById(@PathVariable @Valid @Positive(message = "쿠폰 아이디는 1 이상이어야 합니다.") Long id) {
        CouponDto response = couponService.findCouponById(id);
        return ResponseEntity.ok(response);
    }
}
