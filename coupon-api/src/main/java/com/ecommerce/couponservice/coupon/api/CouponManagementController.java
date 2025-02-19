package com.ecommerce.couponservice.coupon.api;

import com.ecommerce.couponservice.coupon.dto.CouponDto;
import com.ecommerce.couponservice.coupon.dto.CouponRegisterRequestDto;
import com.ecommerce.couponservice.coupon.service.CouponManagementService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/coupons")
@Validated
public class CouponManagementController {

    private final CouponManagementService couponManagementService;

    @PostMapping
    public ResponseEntity<?> register(@RequestBody @Valid CouponRegisterRequestDto couponRegisterRequestDto) {
        CouponDto response = couponManagementService.register(couponRegisterRequestDto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/load-stock-to-redis/{couponId}")
    public ResponseEntity<?> loadCouponStockToRedis(@PathVariable Long couponId) {
        couponManagementService.loadCouponStockToRedis(couponId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CouponDto> findCouponById(@PathVariable @Valid @Positive(message = "쿠폰 아이디는 1 이상이어야 합니다.") Long id) {
        CouponDto response = couponManagementService.findCouponById(id);
        return ResponseEntity.ok(response);
    }
}
