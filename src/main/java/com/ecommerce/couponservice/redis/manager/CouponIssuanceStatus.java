package com.ecommerce.couponservice.redis.manager;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum CouponIssuanceStatus {

    SUCCESS("쿠폰 발급 성공"),
    ALREADY_IN_WAIT_QUEUE("대기열에 존재합니다. 새로 고침하면 순번이 뒤로 밀리니 주의해주세요."),
    NOT_FOUND_COUPON("쿠폰이 존재하지 않습니다."),
    OUT_OF_STOCK("쿠폰이 모두 소진되었습니다."),
    TRANSACTION_FAILED("Redis 트랜잭션 실패"),
    UNKNOWN_ERROR("알 수 없는 오류");

    private final String message;
}
