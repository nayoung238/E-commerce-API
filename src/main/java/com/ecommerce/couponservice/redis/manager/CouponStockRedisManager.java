package com.ecommerce.couponservice.redis.manager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.HashOperations;
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

    public void registerCoupon(Long couponId, String name, Long quantity) {
        String couponKey = getCouponKey(couponId);
        redisTemplate.execute(new SessionCallback<List<Object>>() {
            @Override
            public List<Object> execute(@NotNull RedisOperations operations) throws DataAccessException {
                operations.multi();

                @SuppressWarnings("unchecked")
                HashOperations<String, String, String> hashOps = operations.opsForHash();
                hashOps.put(couponKey, CouponHashName.NAME.name(), name);
                hashOps.put(couponKey, CouponHashName.STOCK.name(), quantity.toString());

                return operations.exec();
            }
        });
    }

    public Optional<Long> getStock(Long couponId) {
        Object value = redisTemplate.opsForHash().get(getCouponKey(couponId), CouponHashName.STOCK.name());
        return Optional.ofNullable(value)
                .map(v -> Long.parseLong(v.toString()));
    }

    public String getCouponName(Long couponId) {
        Object value = redisTemplate.opsForHash().get(getCouponKey(couponId), CouponHashName.NAME.name());
        return value != null ? value.toString() : null;
    }

    public CouponIssuanceStatus decrementStock(Long couponId, Long accountId) {
        return redisTemplate.execute(new SessionCallback<CouponIssuanceStatus>() {
            @Override
            public <K, V> CouponIssuanceStatus execute(RedisOperations<K, V> operations) throws DataAccessException {
                operations.multi();

                @SuppressWarnings("unchecked")
                RedisOperations<String, String> pipelinedOps = (RedisOperations<String, String>) operations;

                String couponKey = getCouponKey(couponId);
                String waitQueueKey = getWaitQueueKey(couponId);
                String enterQueueKey = getEnterQueueKey(couponId);

                pipelinedOps.opsForZSet().score(waitQueueKey, accountId.toString());
                pipelinedOps.opsForHash().increment(couponKey, CouponHashName.STOCK.name(), -1);
                pipelinedOps.opsForZSet().remove(enterQueueKey, accountId.toString());

                List<Object> results = operations.exec();
                if (results == null) {
                    return CouponIssuanceStatus.TRANSACTION_FAILED;
                }

                Double wasInWaitQueue = (Double) results.get(0);
                Long newStock = (Long) results.get(1);
                Long removedFromEnterQueue = (Long) results.get(2);

                if (wasInWaitQueue != null) {
                    return CouponIssuanceStatus.ALREADY_IN_WAIT_QUEUE;
                }
                if (newStock < 0) {
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

                pipelinedOps.opsForHash().increment(getCouponKey(couponId), CouponHashName.STOCK.name(), 1);

                pipelinedOps.exec();
                return null;
            }
        });
    }

    public Set<String> getCouponStockHashKeys() {
        return redisTemplate.keys(COUPON_KEY + "*");
    }
}
