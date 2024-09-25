package com.ecommerce.couponservice.api;

import com.ecommerce.couponservice.domain.coupon.service.CouponIssuanceService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/coupons/issuance")
@Validated
@Slf4j
public class CouponIssuanceController {

    private final CouponIssuanceService couponIssuanceService;

    @PostMapping("/{couponId}/{accountId}")
    public ResponseEntity<?> issueCoupon(@PathVariable @Valid @Positive(message = "쿠폰 아이디는 1 이상이어야 합니다.") Long couponId,
                                         @PathVariable @Valid @Positive(message = "사용자 계정 아이디는 1 이상이어야 합니다.") Long accountId) {
        String response = couponIssuanceService.issueCoupon(couponId, accountId);
        return ResponseEntity.ok(response);
    }
}
