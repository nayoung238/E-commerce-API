package com.ecommerce.couponservice.api;

import com.ecommerce.couponservice.domain.coupon.dto.CouponIssuanceResultDto;
import com.ecommerce.couponservice.domain.coupon.dto.WaitQueuePositionResponseDto;
import com.ecommerce.couponservice.domain.coupon.service.CouponManagementService;
import com.ecommerce.couponservice.domain.coupon.service.CouponQueueService;
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

    @PostMapping("/v1/{couponId}/{accountId}")
    public ResponseEntity<?> issueCoupon(@PathVariable @Valid @Positive(message = "쿠폰 아이디는 1 이상이어야 합니다.") Long couponId,
                                         @PathVariable @Valid @Positive(message = "사용자 계정 아이디는 1 이상이어야 합니다.") Long accountId) {
        CouponIssuanceResultDto response = couponManagementService.issueCouponInDatabase(couponId, accountId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/v2/{couponId}/{accountId}")
    public ResponseEntity<?> addToWaitQueue(@PathVariable @Valid @Positive(message = "쿠폰 아이디는 1 이상이어야 합니다.") Long couponId,
                                                      @PathVariable @Valid @Positive(message = "사용자 계정 아이디는 1 이상이어야 합니다.") Long accountId) {
        WaitQueuePositionResponseDto response = couponQueueService.addToCouponWaitQueue(couponId, accountId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/v2/{couponId}/{accountId}")
    public ResponseEntity<?> getPositionInWaitQueue(@PathVariable @Valid @Positive(message = "쿠폰 아이디는 1 이상이어야 합니다.") Long couponId,
                                                      @PathVariable @Valid @Positive(message = "사용자 계정 아이디는 1 이상이어야 합니다.") Long accountId) {
        WaitQueuePositionResponseDto response = couponQueueService.getPositionInWaitQueue(couponId, accountId);
        return ResponseEntity.ok(response);
    }
}
