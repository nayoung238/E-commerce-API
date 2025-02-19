package com.ecommerce.couponservice.coupon.dto;

import com.ecommerce.couponservice.coupon.entity.Coupon;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;

@Getter
public class CouponDto {

    private Long id;
    private String name;
    private String description;
    private Long itemId;
    private BigDecimal discountRate;
    private Long quantity;

    @Builder(access = AccessLevel.PRIVATE)
    private CouponDto(Long id, String name, String description, Long itemId, BigDecimal discountRate, Long quantity) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.itemId = itemId;
        this.discountRate = discountRate;
        this.quantity = quantity;
    }

    public static CouponDto of(Coupon coupon) {
        return CouponDto.builder()
                .id(coupon.getId())
                .name(coupon.getName())
                .description(coupon.getDescription())
                .itemId(coupon.getItemId())
                .discountRate(coupon.getDiscountRate())
                .quantity(coupon.getQuantity())
                .build();
    }
}
