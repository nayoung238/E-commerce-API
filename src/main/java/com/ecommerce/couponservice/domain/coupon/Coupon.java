package com.ecommerce.couponservice.domain.coupon;

import com.ecommerce.couponservice.domain.coupon.dto.CouponRegisterRequestDto;
import com.ecommerce.couponservice.exception.ExceptionCode;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    @Column(nullable = false)
    private Long itemId;

    @Column(nullable = false, precision = 4, scale = 1)
    @DecimalMin(value = "0.00", inclusive = true, message = "할인율은 0% 이상이어야 합니다.")
    @DecimalMax(value = "100.00", inclusive = true, message = "할인율은 100% 이하여야 합니다.")
    @Digits(integer = 3, fraction = 1, message = "할인율은 최대 3자리 정수와 1자리 소수점만 허용합니다.")
    private BigDecimal discountRate;

    @Column(nullable = false)
    private Long quantity;

    @Builder(access = AccessLevel.PRIVATE)
    private Coupon(String name, String description, Long itemId, BigDecimal discountRate, Long quantity) {
        this.name = name;
        this.description = description;
        this.itemId = itemId;
        this.discountRate = discountRate;
        this.quantity = quantity;
    }

    public static Coupon of(CouponRegisterRequestDto registerRequestDto) {
        return Coupon.builder()
                .name(registerRequestDto.getName())
                .description(registerRequestDto.getDescription())
                .itemId(registerRequestDto.getItemId())
                .discountRate(registerRequestDto.getDiscountRate())
                .quantity(registerRequestDto.getQuantity())
                .build();
    }

    public void decrementQuantity() {
        if(quantity <= 0) {
            throw new IllegalArgumentException(ExceptionCode.COUPON_OUT_OF_STOCK.getMessage());
        }
        this.quantity--;
    }
}
