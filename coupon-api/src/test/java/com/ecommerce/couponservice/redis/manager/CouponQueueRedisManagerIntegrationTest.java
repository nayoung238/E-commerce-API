package com.ecommerce.couponservice.redis.manager;

import com.ecommerce.couponservice.IntegrationTestSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;
import java.util.stream.IntStream;

import static com.ecommerce.couponservice.redis.manager.CouponQueueRedisManager.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CouponQueueRedisManagerIntegrationTest extends IntegrationTestSupport {

    @Autowired
    protected CouponQueueRedisManager couponQueueRedisManager;

    @Autowired
    protected RedisTemplate<String, String> redisTemplate;

    @AfterEach
    void clearTestData() {
        couponQueueRedisManager.getWaitQueueKeys().forEach(key -> redisTemplate.delete(key));
        couponQueueRedisManager.getEnterQueueKeys().forEach(key -> redisTemplate.delete(key));
    }

    @DisplayName("Batch 사이즈보다 적은 사용자가 단일 쿠폰 발급 요청 시 모든 사용자를 한 번에 Enter Queue에 추가해야 한다.")
    @Test
    void shouldAddAllUsersToEnterQueueAtOnceWhenRequestIsBelowBatchSize() {
        // given
        Long couponId = 1L;
        List<Long> accountIds = IntStream.rangeClosed(1, Math.max((int) (BATCH_SIZE - 2), 1))
                .mapToObj(Long:: valueOf)
                .toList();

        // when
        accountIds.forEach(i -> couponQueueRedisManager.addCouponWaitQueue(couponId, i));

        // then
        long movedCount = couponQueueRedisManager.moveFromWaitToEnterQueue(couponId);
        assertEquals(accountIds.size(), movedCount);
        assertThat(redisTemplate.opsForZSet().size(getWaitQueueKey(couponId))).isZero();
        assertThat(redisTemplate.opsForZSet().size(getEnterQueueKey(couponId))).isEqualTo(accountIds.size());
    }

    @DisplayName("Batch 사이즈보다 많은 사용자가 단일 쿠폰 발급 요청 시 Batch 단위로 Enter Queue에 추가해야 한다.")
    @Test
    void shouldAddUsersToEnterQueueInBatchesWhenRequestExceedsBatchSize() {
        // given
        Long couponId = 1L;
        List<Long> accountIds = IntStream.rangeClosed(1, (int) (BATCH_SIZE + 3))
                .mapToObj(Long:: valueOf)
                .toList();

        // when
        accountIds.forEach(i -> couponQueueRedisManager.addCouponWaitQueue(couponId, i));

        // then
        long movedCount = couponQueueRedisManager.moveFromWaitToEnterQueue(couponId);
        assertEquals(BATCH_SIZE, movedCount);
        assertThat(redisTemplate.opsForZSet().size(getWaitQueueKey(couponId))).isEqualTo(accountIds.size() - BATCH_SIZE);

        movedCount += couponQueueRedisManager.moveFromWaitToEnterQueue(couponId);
        assertEquals(accountIds.size(), movedCount);
        assertThat(redisTemplate.opsForZSet().size(getWaitQueueKey(couponId))).isZero();

        assertThat(redisTemplate.opsForZSet().size(getEnterQueueKey(couponId))).isEqualTo(accountIds.size());
    }

    @DisplayName("다수 쿠폰에 대한 Wait Queue에 있는 사용자를 Enter Queue로 이동시켜야 한다.")
    @Test
    void shouldMoveUsersFromWaitToEnterQueueForMultipleCoupons() {
        // given
        List<Long> couponIds = List.of(1L, 2L);
        final long accountId = 1L;

        // when
        couponIds.forEach(i -> couponQueueRedisManager.addCouponWaitQueue(i, accountId));

        // then
        couponIds.forEach(id -> {
            long movedCount = couponQueueRedisManager.moveFromWaitToEnterQueue(id);
            assertEquals(1, movedCount);
            assertThat(redisTemplate.opsForZSet().size(getWaitQueueKey(id))).isZero();
            assertThat(redisTemplate.opsForZSet().size(getEnterQueueKey(id))).isEqualTo(1);
        });
    }

    private String getWaitQueueKey(Long couponId) {
        return WAIT_KEY_PREFIX + couponId;
    }

    private String getEnterQueueKey(Long couponId) {
        return ENTER_KEY_PREFIX + couponId;
    }
}