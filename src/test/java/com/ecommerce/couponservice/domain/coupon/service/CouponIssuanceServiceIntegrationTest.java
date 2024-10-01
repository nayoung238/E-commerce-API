package com.ecommerce.couponservice.domain.coupon.service;

import com.ecommerce.couponservice.IntegrationTestSupport;
import com.ecommerce.couponservice.domain.coupon.dto.CouponIssuanceResultDto;
import com.ecommerce.couponservice.redis.manager.BaseRedisManager;
import com.ecommerce.couponservice.redis.manager.CouponIssuanceStatus;
import com.ecommerce.couponservice.redis.manager.CouponQueueRedisManager;
import com.ecommerce.couponservice.redis.manager.CouponStockRedisManager;
import com.ecommerce.couponservice.redis.scheduler.MoveFromWaitToEnterQueueScheduler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class CouponIssuanceServiceIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private CouponIssuanceService couponIssuanceService;

    @Autowired
    private CouponStockRedisManager couponStockRedisManager;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @MockBean
    private MoveFromWaitToEnterQueueScheduler moveFromWaitToEnterQueueScheduler;

    @Autowired
    private CouponQueueRedisManager couponQueueRedisManager;

    @AfterEach
    void clearTestData() {
        couponStockRedisManager.getCouponStockHashKeys().forEach(key -> redisTemplate.delete(key));
        couponQueueRedisManager.getWaitQueueKeys().forEach(key -> redisTemplate.delete(key));
        couponQueueRedisManager.getEnterQueueKeys().forEach(key -> redisTemplate.delete(key));
    }

    @DisplayName("쿠폰 수량이 있으면 Redis에서 수량 감소가 처리되어야 한다.")
    @Test
    void redisCouponStockDecrementTest() {
        // given
        final long couponId = 1L;
        final long quantity = 10L;
        final Long accountId = 1L;
        final String ENTER_QUEUE_KEY = BaseRedisManager.getEnterQueueKey(couponId);
        couponStockRedisManager.registerCouponStock(couponId, quantity);
        redisTemplate.opsForZSet().add(ENTER_QUEUE_KEY, accountId.toString(), (double) System.currentTimeMillis()); // enter-queue 추가

        // when
        CouponIssuanceResultDto response = couponIssuanceService.issueCoupon(couponId, accountId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(CouponIssuanceStatus.SUCCESS);
        assertThat(couponStockRedisManager.getStock(couponId)).isEqualTo(quantity - 1);
        assertThat(redisTemplate.opsForZSet().size(ENTER_QUEUE_KEY)).isZero();
    }

    @DisplayName("레디스에 쿠폰 수량이 없다면 쿠폰을 발급하지 않는다.")
    @Test
    void redisCouponStockDecrementShouldNotAllowNegativeTest() {
        // given
        final Long couponId = 1L;
        final long accountId = 1L;
        couponStockRedisManager.registerCouponStock(couponId, 0L);

        // when
        CouponIssuanceResultDto response = couponIssuanceService.issueCoupon(couponId, accountId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(CouponIssuanceStatus.OUT_OF_STOCK);
        assertThat(redisTemplate.opsForHash().get(BaseRedisManager.COUPON_STOCK_KEY, couponId.toString())).isEqualTo("0");
    }

    @DisplayName("레디스에 쿠폰 정보가 없으면 쿠폰을 발급하지 않는다.")
    @Test
    void shouldSkipWhenCouponInfoNotExists() {
        // given
        final Long couponId = 1L;
        final long accountId = 1L;

        // when
        CouponIssuanceResultDto response = couponIssuanceService.issueCoupon(couponId, accountId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(CouponIssuanceStatus.NOT_FOUND_COUPON);
        assertThat(redisTemplate.opsForHash().get(BaseRedisManager.COUPON_STOCK_KEY, couponId.toString())).isEqualTo("0");
    }

    @DisplayName("사용자가 wait-queue에 있다면 쿠폰을 발급하지 않는다.")
    @Test
    void sholudNotIssueWhenUserInWaitQueue() {
        // given
        final Long couponId = 1L;
        final long accountId = 1L;

        Mockito.doNothing().when(moveFromWaitToEnterQueueScheduler).processScheduledWaitQueueTasks();

        // when
        couponQueueRedisManager.addCouponWaitQueue(couponId, accountId);
        CouponIssuanceResultDto response = couponIssuanceService.issueCoupon(couponId, accountId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(CouponIssuanceStatus.ALREADY_IN_WAIT_QUEUE);
        assertThat(redisTemplate.opsForHash().get(BaseRedisManager.COUPON_STOCK_KEY, couponId.toString())).isEqualTo("0");
    }
}