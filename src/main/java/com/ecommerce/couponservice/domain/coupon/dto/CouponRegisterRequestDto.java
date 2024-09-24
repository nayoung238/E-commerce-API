package com.ecommerce.couponservice.domain.coupon.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class CouponRegisterRequestDto {

    @NotBlank(message = "쿠폰명은 필수입니다.")
    private String name;

    private String description;

    @NotNull(message = "상품 아이디는 필수입니다.")
    @Min(value = 1, message = "상품 아이디는 1 이상이어야 합니다.")
    private Long itemId;

    @DecimalMin(value = "0.00", inclusive = true, message = "할인율은 0% 이상이어야 합니다.")
    @DecimalMax(value = "100.00", inclusive = true, message = "할인율은 100% 이하여야 합니다.")
    @Digits(integer = 3, fraction = 1, message = "할인율은 최대 3자리 정수와 1자리 소수점만 허용합니다.")
    private BigDecimal discountRate;

    @NotNull(message = "쿠폰 수량은 필수입니다.")
    @Min(value = 0, message = "쿠폰 수량은 0 이상이어야 합니다.")
    private Long quantity;
}
