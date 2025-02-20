package com.ecommerce.couponservice.coupon.entity;

import com.ecommerce.couponservice.coupon.dto.CouponRegisterRequestDto;
import com.ecommerce.couponservice.couponlog.entity.CouponLog;
import com.ecommerce.couponservice.redis.manager.CouponIssuanceStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "coupon_id")
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

    @OneToMany(mappedBy = "coupon", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CouponLog> couponLogs;

    public static Coupon of(CouponRegisterRequestDto registerRequestDto) {
        return Coupon.builder()
                .name(registerRequestDto.getName())
                .description(registerRequestDto.getDescription())
                .itemId(registerRequestDto.getItemId())
                .discountRate(registerRequestDto.getDiscountRate())
                .quantity(registerRequestDto.getQuantity())
                .build();
    }

    public CouponIssuanceStatus decrementQuantity() {
        if (quantity <= 0) {
            return CouponIssuanceStatus.OUT_OF_STOCK;
        }
        this.quantity--;
        return CouponIssuanceStatus.SUCCESS;
    }
}
