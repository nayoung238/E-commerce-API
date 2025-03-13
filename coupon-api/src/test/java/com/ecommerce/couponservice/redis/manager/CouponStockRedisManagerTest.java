package com.ecommerce.couponservice.redis.manager;

import com.ecommerce.couponservice.IntegrationTestSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class CouponStockRedisManagerTest extends IntegrationTestSupport {

    @Autowired
    private CouponStockRedisManager couponStockRedisManager;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private CouponQueueRedisManager couponQueueRedisManager;

    @AfterEach
    void clearTestData() {
        couponStockRedisManager.getCouponStockHashKeys().forEach(key -> redisTemplate.delete(key));
        couponQueueRedisManager.getWaitQueueKeys().forEach(key -> redisTemplate.delete(key));
        couponQueueRedisManager.getEnterQueueKeys().forEach(key -> redisTemplate.delete(key));
    }

    @DisplayName("[Redis 데이터 변경 테스트] 쿠폰 수량이 있으면 레디스에서 수량 감소 처리")
    @Test
    void redis_coupon_stock_decrement_success_test() {
        // given
        final long couponId = 10L;
        final long quantity = 10L;
        final Long userId = 1L;
        final String ENTER_QUEUE_KEY = BaseRedisManager.getEnterQueueKey(couponId);
        couponStockRedisManager.registerCoupon(couponId,"coupon-name", quantity);
        redisTemplate.opsForZSet().add(ENTER_QUEUE_KEY, userId.toString(), (double) System.currentTimeMillis()); // enter-queue 추가

        // when
		Map<String, Object> response = couponStockRedisManager.issueCouponAndPublishEvent(couponId, userId);

        // then
        assertThat(response).isNotNull();
		Assertions.assertTrue(response.containsKey("status"));
		assertThat(response.get("status")).isEqualTo("SUCCESS");

		Assertions.assertTrue(response.containsKey("couponId"));
		assertThat(Long.valueOf(response.get("couponId").toString())).isEqualTo(couponId);

		Assertions.assertTrue(response.containsKey("newStock"));
		assertThat(Long.valueOf(response.get("newStock").toString())).isEqualTo(quantity - 1);

        Optional<Long> expectedQuantity = couponStockRedisManager.getStock(couponId);
        assert expectedQuantity.isPresent();
        assertThat(expectedQuantity.get()).isEqualTo(quantity - 1);
        assertThat(redisTemplate.opsForZSet().size(ENTER_QUEUE_KEY)).isZero();
    }

    @DisplayName("[쿠폰 발급 실패 테스트] 레디스에 쿠폰 수량이 없다면 쿠폰을 발급하지 않는다.")
    @Test
    void issue_coupon_failed_test_when_out_of_stock() {
		// given
		final Long couponId = 20L;
		final long userId = 1L;
		couponStockRedisManager.registerCoupon(couponId, "coupon-name", 0L);

		// when
		Map<String, Object> response = couponStockRedisManager.issueCouponAndPublishEvent(couponId, userId);

		// then
		assertThat(response).isNotNull();
		Assertions.assertTrue(response.containsKey("status"));
		assertThat(response.get("status")).isEqualTo("FAILED");

		Assertions.assertTrue(response.containsKey("reason"));
		assertThat(response.get("reason")).isEqualTo("Out of stock: Insufficient coupon inventory");
	}

    @DisplayName("[쿠폰 발급 실패 테스트] 레디스에 쿠폰 정보가 없으면 쿠폰을 발급하지 않는다.")
    @Test
    void issue_coupon_failed_test_when_coupon_not_exists() {
        // given
        final Long couponId = 30L;
        final long userId = 1L;

        // when
		Map<String, Object> response = couponStockRedisManager.issueCouponAndPublishEvent(couponId, userId);

        // then
		assertThat(response).isNotNull();
		Assertions.assertTrue(response.containsKey("status"));
		assertThat(response.get("status")).isEqualTo("FAILED");

		Assertions.assertTrue(response.containsKey("reason"));
		assertThat(response.get("reason")).isEqualTo("Out of stock: Insufficient coupon inventory");
	}

    @DisplayName("[쿠폰 발급 실패 테스트] 사용자가 대기큐에 있다면 쿠폰을 발급하지 않는다.")
    @Test
    void issue_coupon_failed_test_when_user_in_wait_queue() {
        // given
        final Long couponId = 40L;
        final long userId = 1L;

        // when
        couponQueueRedisManager.addCouponWaitQueue(couponId, userId);
		Map<String, Object> response =couponStockRedisManager.issueCouponAndPublishEvent(couponId, userId);

        // then
		assertThat(response).isNotNull();
		Assertions.assertTrue(response.containsKey("status"));
		assertThat(response.get("status")).isEqualTo("ERROR");

		Assertions.assertTrue(response.containsKey("reason"));
		assertThat(response.get("reason")).isEqualTo("Invalid queue state: User is in the wait queue.");
    }
}