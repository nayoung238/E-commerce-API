package com.ecommerce.couponservice.redis.manager;

import com.ecommerce.couponservice.common.exception.CustomRedisException;
import com.ecommerce.couponservice.common.exception.ErrorCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Component
@RequiredArgsConstructor
@Slf4j
public class CouponQueueRedisManager extends BaseRedisManager {

    private final RedisTemplate<String, String> redisTemplate;
    private final long START_INDEX = 0L;
    public static final long BATCH_SIZE = 10L;

    public void addCouponWaitQueue(Long couponId, Long userId) {
        String waitKey = getWaitQueueKey(couponId);
        addZSet(waitKey, userId.toString());
    }

    private void addZSet(String key, String value) {
        redisTemplate
                .opsForZSet()
                .add(key, value, (double) System.currentTimeMillis());
    }

    public Long getWaitQueueRank(Long couponId, Long userId) {
        String waitKey = getWaitQueueKey(couponId);
        if(Objects.equals(Boolean.FALSE, redisTemplate.hasKey(waitKey))) {
            throw new CustomRedisException(ErrorCode.WAIT_QUEUE_NOT_FOUND);
        }
        return redisTemplate.opsForZSet().rank(waitKey, userId.toString());
    }

    public long moveFromWaitToEnterQueue(Long couponId) {
        String waitQueueKey = getWaitQueueKey(couponId);
        String enterQueueKey = getEnterQueueKey(couponId);
        AtomicLong movedCount = new AtomicLong(0);

        Set<ZSetOperations.TypedTuple<String>> topWaitingUserIds = redisTemplate
                .opsForZSet()
                .rangeWithScores(waitQueueKey, START_INDEX, BATCH_SIZE - 1);

        if (topWaitingUserIds!= null && !topWaitingUserIds.isEmpty()) {
            List txResults = redisTemplate.execute(new SessionCallback<>() {

                @Override
                public <K, V> List execute(@NonNull RedisOperations<K, V> operations) throws DataAccessException {
                    operations.multi();
                    RedisOperations<String, String> pipelinedOps = (RedisOperations<String, String>) operations;
                    pipelinedOps.opsForZSet().add(enterQueueKey, topWaitingUserIds);

                    topWaitingUserIds.forEach(tuple -> pipelinedOps.opsForZSet().remove(waitQueueKey, tuple.getValue()));
                    return operations.exec();
                }
            });

            if (txResults != null && !txResults.isEmpty()) {
                movedCount.set(topWaitingUserIds.size());
                log.info("Moved {} users from wait queue to enter queue", movedCount.get());
            } else {
                log.warn("Transaction failed or no items were moved");
            }
        }
        return movedCount.get();
    }

    public Set<String> getWaitQueueKeys() {
        String pattern = WAIT_KEY_PREFIX + "*";
        return redisTemplate.keys(pattern);

//        this.redisTemplate.execute((RedisCallback<Set<String>>) connection -> {
//            Cursor<byte[]> cursor = connection.scan(ScanOptions.scanOptions().match(pattern).build());
//            while (cursor.hasNext()) {
//                keys.add(new String(cursor.next()));
//            }
//            return keys;
//        });
//
//        return keys;
    }

    public Set<String> getEnterQueueKeys() {
        String pattern = ENTER_KEY_PREFIX + "*";
        return redisTemplate.keys(pattern);
    }

    public Set<ZSetOperations.TypedTuple<String>> getEnterQueueValueAndScore(Long couponId) {
        String enterQueueKey = getEnterQueueKey(couponId);
        return redisTemplate
                .opsForZSet()
                .rangeWithScores(enterQueueKey, START_INDEX, BATCH_SIZE - 1);
    }

    public void removeEnterQueueValue(Long couponId, Long userId) {
        String enterQueueKey = getEnterQueueKey(couponId);
        removeZSet(enterQueueKey, userId.toString());
    }

    private void removeZSet(String key, String value) {
        redisTemplate
                .opsForZSet()
                .remove(key, value);
    }
}