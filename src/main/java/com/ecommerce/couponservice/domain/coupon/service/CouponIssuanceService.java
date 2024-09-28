package com.ecommerce.couponservice.domain.coupon.service;

import com.ecommerce.couponservice.domain.coupon.Coupon;
import com.ecommerce.couponservice.domain.coupon.dto.WaitQueuePositionResponseDto;
import com.ecommerce.couponservice.domain.coupon.repo.CouponRepository;
import com.ecommerce.couponservice.exception.CustomRedisException;
import com.ecommerce.couponservice.exception.ExceptionCode;
import com.ecommerce.couponservice.internalevent.couponissuanceresult.CouponIssuanceResultInternalEvent;
import com.ecommerce.couponservice.internalevent.service.InternalEventService;
import com.ecommerce.couponservice.domain.coupon.repo.CouponRedisRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CouponIssuanceService {

    private final CouponRepository couponRepository;
    private final InternalEventService internalEventService;
    private final CouponRedisRepository couponRedisRepository;

    @Transactional
    public String issueCoupon(Long couponId, Long accountId) {
        try {
            Optional<Coupon> coupon = couponRepository.findByIdWithPessimisticLock(couponId);
            if (coupon.isEmpty()) {
                throw new EntityNotFoundException(ExceptionCode.NOT_FOUND_COUPON.getMessage());
            }
            coupon.get().decrementQuantity();
            internalEventService.publishInternalEvent(CouponIssuanceResultInternalEvent.init(couponId, accountId));
            return "Coupon issued successfully !!";
        } catch (EntityNotFoundException | IllegalArgumentException e) {
            throw e;
        }
    }

    public WaitQueuePositionResponseDto addToCouponWaitQueue(Long couponId, Long accountId) {
        boolean exists = couponRepository.existsById(couponId);
        if(!exists) {
            return WaitQueuePositionResponseDto.waitQueueNotFound(couponId);
        }
        couponRedisRepository.addCouponWaitQueue(couponId, accountId);
        return getPositionInWaitQueue(couponId, accountId);
    }

    public WaitQueuePositionResponseDto getPositionInWaitQueue(Long couponId, Long accountId) {
        try {
            Long rank = couponRedisRepository.getWaitQueueRank(couponId, accountId);
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
