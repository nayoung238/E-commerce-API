package com.ecommerce.couponservice.redis.scheduler;

import com.ecommerce.couponservice.domain.coupon.repo.CouponRedisRepository;
import com.ecommerce.couponservice.domain.coupon.service.CouponIssuanceService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class CouponIssuanceScheduler extends BaseCouponScheduler {

    private final CouponRedisRepository couponRedisRepository;
    private final CouponIssuanceService couponIssuanceService;

    @Scheduled(fixedDelay = 5_000, initialDelay = 13_000)
    private void processScheduleEnterQueueTasks() {
        Set<String> enterKeys = couponRedisRepository.getEnterQueueKeys();
        if(enterKeys.isEmpty()) {
            log.debug("No enter queues found to process.");
            return;
        }

        for(String enterKey: enterKeys) {
            processEnterQueue(enterKey);
        }
    }

    private void processEnterQueue(String enterKey) {
        try {
            Long couponId = extractCouponId(CouponRedisRepository.ENTER_KEY_PREFIX, enterKey);
            if(couponId == null) {
                log.warn("Invalid enter queue key format: {}", enterKey);
                return;
            }

            List<Long> accountIds = getAccountIdsFromEnterQueue(couponId);
            if(accountIds.isEmpty()) {
                log.warn("No valid accountIds found for couponId: {}", couponId);
                return;
            }
            accountIds.forEach(i -> issueCouponsInBatch(couponId, i));

        } catch (NumberFormatException e) {
            log.error("Failed to parse enter queue key: {}", enterKey, e);
        }  catch (Exception e) {
            log.error("Failed to process enter queue key: {}", enterKey, e);
        }
    }

    List<Long> getAccountIdsFromEnterQueue(Long couponId) {
        return couponRedisRepository.getEnterQueueValueAndScore(couponId)
                .stream()
                .map(ZSetOperations.TypedTuple::getValue)
                .filter(Objects::nonNull)
                .map(Long::parseLong)
                .toList();
    }

    private void issueCouponsInBatch(long couponId, long accountId) {
        try {
            couponIssuanceService.issueCoupon(couponId, accountId);
        } catch (EntityNotFoundException e) {
            log.error("Coupon not found: {}", couponId, e);
        } catch (IllegalArgumentException e) {
            log.error("All coupons({}) have been redeemed.", couponId, e);
        } finally {
            couponRedisRepository.removeEnterQueueValue(couponId, accountId);
        }
    }
}
