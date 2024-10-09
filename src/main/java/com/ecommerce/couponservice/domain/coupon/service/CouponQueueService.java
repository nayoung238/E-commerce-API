package com.ecommerce.couponservice.domain.coupon.service;

import com.ecommerce.couponservice.domain.coupon.dto.CouponIssuanceResultDto;
import com.ecommerce.couponservice.domain.coupon.dto.WaitQueuePositionResponseDto;
import com.ecommerce.couponservice.exception.CustomRedisException;
import com.ecommerce.couponservice.exception.ExceptionCode;
import com.ecommerce.couponservice.internalevent.couponissuanceresult.CouponIssuanceResultInternalEvent;
import com.ecommerce.couponservice.internalevent.service.InternalEventService;
import com.ecommerce.couponservice.redis.manager.CouponIssuanceStatus;
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

    private final InternalEventService internalEventService;
    private final CouponQueueRedisManager couponQueueRedisManager;
    private final CouponStockRedisManager couponStockRedisManager;

    public CouponIssuanceResultDto issueCoupon(Long couponId, Long accountId) {
        CouponIssuanceStatus status = couponStockRedisManager.decrementStock(couponId, accountId);
        if (status == CouponIssuanceStatus.SUCCESS) {
            internalEventService.publishInternalEvent(CouponIssuanceResultInternalEvent.init(couponId, accountId));
        }
        else {
            couponStockRedisManager.revertDecrementOperation(couponId);
        }
        return CouponIssuanceResultDto.of(couponId, accountId, status);
    }

    public WaitQueuePositionResponseDto addToCouponWaitQueue(Long couponId, Long accountId) {
        Optional<Long> stock = couponStockRedisManager.getStock(couponId);    // Redis에 load된 쿠폰만 대기열 진행
        if(stock.isEmpty()) {
            return WaitQueuePositionResponseDto.waitQueueNotFound(couponId);
        }
        couponQueueRedisManager.addCouponWaitQueue(couponId, accountId);
        return getPositionInWaitQueue(couponId, accountId);
    }

    public WaitQueuePositionResponseDto getPositionInWaitQueue(Long couponId, Long accountId) {
        try {
            Long rank = couponQueueRedisManager.getWaitQueueRank(couponId, accountId);
            if(rank == null) {
                return WaitQueuePositionResponseDto.accountIdNotInWaitQueue(couponId, accountId);
            }
            Long position = rank + 1;
            return WaitQueuePositionResponseDto.accountIdInWaitQueue(couponId, accountId, position);
        } catch (CustomRedisException e) {
            if (e.getExceptionCode() == ExceptionCode.WAIT_QUEUE_NOT_FOUND) {
                return WaitQueuePositionResponseDto.waitQueueNotFound(couponId);
            }
            return WaitQueuePositionResponseDto.unexpectedError();
        } catch (Exception e) {
            return WaitQueuePositionResponseDto.unexpectedError();
        }
    }
}
