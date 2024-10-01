package com.ecommerce.couponservice.redis.manager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class CouponStockRedisManager extends BaseRedisManager {

    private final RedisTemplate<String, String> redisTemplate;

    public void registerCouponStock(Long couponId, Long quantity) {
        redisTemplate.opsForHash()
                .put(COUPON_STOCK_KEY, couponId.toString(), quantity.toString());
    }

    public Long getStock(Long couponId) {
        Object value = redisTemplate.opsForHash().get(COUPON_STOCK_KEY, couponId.toString());
        return Optional.ofNullable(value)
                .map(v -> Long.parseLong(v.toString()))
                .orElse(0L);
    }

    public CouponIssuanceStatus decrementStock(Long couponId, Long accountId) {
        return redisTemplate.execute(new SessionCallback<CouponIssuanceStatus>() {

            @Override
            public <K, V> CouponIssuanceStatus execute(RedisOperations<K, V> operations) throws DataAccessException {
                operations.multi();
                RedisOperations<String, String> pipelinedOps = (RedisOperations<String, String>) operations;

                String waitQueueKey = getWaitQueueKey(couponId);
                String enterQueueKey = getEnterQueueKey(couponId);

                pipelinedOps.opsForZSet().score(waitQueueKey, accountId.toString());
                pipelinedOps.opsForHash().get(COUPON_STOCK_KEY, couponId.toString());
                pipelinedOps.opsForHash().increment(COUPON_STOCK_KEY, couponId.toString(), -1);
                pipelinedOps.opsForZSet().remove(enterQueueKey, accountId.toString());

                List<Object> results = operations.exec();
                if (results == null) {
                    return CouponIssuanceStatus.TRANSACTION_FAILED;
                }

                Double wasInWaitQueue = (Double) results.get(0);
                String currentStockStr = (String) results.get(1);
                Long newStock = (Long) results.get(2);
                Long removedFromEnterQueue = (Long) results.get(3);

                if (wasInWaitQueue != null) {
                    return CouponIssuanceStatus.ALREADY_IN_WAIT_QUEUE;
                }
                if (currentStockStr == null) {
                    return CouponIssuanceStatus.NOT_FOUND_COUPON;
                }
                Long currentStock = parseLongSafely(currentStockStr);
                if (currentStock == null) {
                    return CouponIssuanceStatus.UNKNOWN_ERROR;
                }
                if (currentStock <= 0 || newStock < 0) {
                    return CouponIssuanceStatus.OUT_OF_STOCK;
                }
                if (removedFromEnterQueue == 1) {
                    return CouponIssuanceStatus.SUCCESS;
                }
                return CouponIssuanceStatus.UNKNOWN_ERROR;
            }
        });
    }

    public void revertDecrementOperation(Long couponId) {
        redisTemplate.execute(new SessionCallback<Void>() {
            @Override
            public <K, V> Void execute(RedisOperations<K, V> operations) throws DataAccessException {
                RedisOperations<String, String> pipelinedOps = (RedisOperations<String, String>) operations;
                pipelinedOps.multi();

                pipelinedOps.opsForHash().increment(COUPON_STOCK_KEY, couponId.toString(), 1);

                pipelinedOps.exec();
                return null;
            }
        });
    }

    private Long parseLongSafely(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public Set<String> getCouponStockHashKeys() {
        return redisTemplate.keys(COUPON_STOCK_KEY + "*");
    }
}
