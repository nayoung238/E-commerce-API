package com.ecommerce.couponservice.coupon.service;

import com.ecommerce.couponservice.coupon.dto.WaitQueuePositionResponseDto;
import com.ecommerce.couponservice.common.exception.CustomRedisException;
import com.ecommerce.couponservice.common.exception.ErrorCode;
import com.ecommerce.couponservice.redis.manager.CouponQueueRedisManager;
import com.ecommerce.couponservice.redis.manager.CouponStockRedisManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CouponQueueService {

    private final CouponQueueRedisManager couponQueueRedisManager;
    private final CouponStockRedisManager couponStockRedisManager;

    public WaitQueuePositionResponseDto addToCouponWaitQueue(Long couponId, Long userId) {
        Optional<Long> stock = couponStockRedisManager.getStock(couponId);    // Redis에 load된 쿠폰만 대기열 진행
        if(stock.isEmpty()) {
            return WaitQueuePositionResponseDto.waitQueueNotFound(couponId);
        }
        couponQueueRedisManager.addCouponWaitQueue(couponId, userId);
        return getPositionInWaitQueue(couponId, userId);
    }

    public WaitQueuePositionResponseDto getPositionInWaitQueue(Long couponId, Long userId) {
        try {
            Long rank = couponQueueRedisManager.getWaitQueueRank(couponId, userId);
            if(rank == null) {
                return WaitQueuePositionResponseDto.userIdNotInWaitQueue(couponId, userId);
            }
            Long position = rank + 1;
            return WaitQueuePositionResponseDto.userIdInWaitQueue(couponId, userId, position);
        } catch (CustomRedisException e) {
            if (e.getErrorCode() == ErrorCode.WAIT_QUEUE_NOT_FOUND) {
                return WaitQueuePositionResponseDto.waitQueueNotFound(couponId);
            }
            return WaitQueuePositionResponseDto.unexpectedError();
        } catch (Exception e) {
            return WaitQueuePositionResponseDto.unexpectedError();
        }
    }
}
