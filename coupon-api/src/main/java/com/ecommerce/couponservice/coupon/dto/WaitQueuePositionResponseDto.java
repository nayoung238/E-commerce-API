package com.ecommerce.couponservice.coupon.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
public class WaitQueuePositionResponseDto {

    private boolean exists;
    private Long position;
    private String message;

    @Builder(access = AccessLevel.PRIVATE)
    private WaitQueuePositionResponseDto(boolean exists, Long position, String message) {
        this.exists = exists;
        this.position = position;
        this.message = message;
    }

    public static WaitQueuePositionResponseDto waitQueueNotFound(Long couponId) {
        return WaitQueuePositionResponseDto.builder()
                .exists(false)
                .position(null)
                .message(String.format("The wait queue for coupon %d does not exist", couponId))
                .build();
    }

    public static WaitQueuePositionResponseDto accountIdNotInWaitQueue(Long couponId, Long accountId) {
        return WaitQueuePositionResponseDto.builder()
                .exists(false)
                .position(null)
                .message(String.format("The wait queue for coupon %d does not contain account %d", couponId, accountId))
                .build();
    }

    public static WaitQueuePositionResponseDto accountIdInWaitQueue(Long couponId, Long accountId, Long position) {
        return WaitQueuePositionResponseDto.builder()
                .exists(true)
                .position(position)
                .message(String.format("Account %d is in position %d for coupon %d", accountId, position, couponId))
                .build();
    }

    public static WaitQueuePositionResponseDto unexpectedError() {
        return WaitQueuePositionResponseDto.builder()
                .exists(false)
                .position(null)
                .message("The service is temporarily unavailable.")
                .build();
    }
}
