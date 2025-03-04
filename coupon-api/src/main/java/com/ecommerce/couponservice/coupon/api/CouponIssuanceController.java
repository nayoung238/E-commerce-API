package com.ecommerce.couponservice.coupon.api;

import com.ecommerce.couponservice.coupon.dto.CouponIssuanceResultDto;
import com.ecommerce.couponservice.coupon.dto.WaitQueuePositionResponseDto;
import com.ecommerce.couponservice.coupon.service.CouponManagementService;
import com.ecommerce.couponservice.coupon.service.CouponQueueService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/coupons/issuance")
@Validated
@Slf4j
public class CouponIssuanceController {

    private final CouponManagementService couponManagementService;
    private final CouponQueueService couponQueueService;

    @PostMapping("/v1/{couponId}/{userId}")
    public ResponseEntity<?> issueCoupon(@PathVariable @Valid @Positive(message = "쿠폰 아이디는 1 이상이어야 합니다.") Long couponId,
                                         @PathVariable @Valid @Positive(message = "유저 아이디는 1 이상이어야 합니다.") Long userId) {
        CouponIssuanceResultDto response = couponManagementService.issueCouponInDatabase(couponId, userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/v2/{couponId}/{userId}")
    public ResponseEntity<?> addToWaitQueue(@PathVariable @Valid @Positive(message = "쿠폰 아이디는 1 이상이어야 합니다.") Long couponId,
                                            @PathVariable @Valid @Positive(message = "유저 아이디는 1 이상이어야 합니다.") Long userId) {
        WaitQueuePositionResponseDto response = couponQueueService.addToCouponWaitQueue(couponId, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/v2/{couponId}/{userId}")
    public ResponseEntity<?> getPositionInWaitQueue(@PathVariable @Valid @Positive(message = "쿠폰 아이디는 1 이상이어야 합니다.") Long couponId,
                                                    @PathVariable @Valid @Positive(message = "유저 아이디는 1 이상이어야 합니다.") Long userId) {
        WaitQueuePositionResponseDto response = couponQueueService.getPositionInWaitQueue(couponId, userId);
        return ResponseEntity.ok(response);
    }
}
