package com.ecommerce.couponservice.redis.manager;

//import com.ecommerce.couponservice.IntegrationTestSupport;
//import com.ecommerce.couponservice.redis.scheduler.MoveFromWaitToEnterQueueScheduler;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mockito;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.data.redis.core.RedisTemplate;
//
//import java.util.Optional;
//
//import static org.assertj.core.api.Assertions.*;
//
//@SpringBootTest
//class CouponStockRedisManagerTest extends IntegrationTestSupport {
//
//    @Autowired
//    private CouponStockRedisManager couponStockRedisManager;
//
//    @Autowired
//    private RedisTemplate<String, String> redisTemplate;
//
//    @MockBean
//    private MoveFromWaitToEnterQueueScheduler moveFromWaitToEnterQueueScheduler;
//
//    @Autowired
//    private CouponQueueRedisManager couponQueueRedisManager;
//
//    @AfterEach
//    void clearTestData() {
//        couponStockRedisManager.getCouponStockHashKeys().forEach(key -> redisTemplate.delete(key));
//        couponQueueRedisManager.getWaitQueueKeys().forEach(key -> redisTemplate.delete(key));
//        couponQueueRedisManager.getEnterQueueKeys().forEach(key -> redisTemplate.delete(key));
//    }
//
//    @DisplayName("쿠폰 수량이 있으면 Redis에서 수량 감소가 처리되어야 한다.")
//    @Test
//    void redisCouponStockDecrementTest() {
//        // given
//        final long couponId = 1L;
//        final long quantity = 10L;
//        final Long userId = 1L;
//        final String ENTER_QUEUE_KEY = BaseRedisManager.getEnterQueueKey(couponId);
//        couponStockRedisManager.registerCoupon(couponId,"coupon-name", quantity);
//        redisTemplate.opsForZSet().add(ENTER_QUEUE_KEY, userId.toString(), (double) System.currentTimeMillis()); // enter-queue 추가
//
//        // when
//        CouponIssuanceStatus response = couponStockRedisManager.decrementStock(couponId, userId);
//
//        // then
//        assertThat(response).isNotNull();
//        assertThat(response).isEqualTo(CouponIssuanceStatus.SUCCESS);
//
//        Optional<Long> expectedQuantity = couponStockRedisManager.getStock(couponId);
//        assert expectedQuantity.isPresent();
//        assertThat(expectedQuantity.get()).isEqualTo(quantity - 1);
//        assertThat(redisTemplate.opsForZSet().size(ENTER_QUEUE_KEY)).isZero();
//    }
//
//    @DisplayName("레디스에 쿠폰 수량이 없다면 쿠폰을 발급하지 않는다.")
//    @Test
//    void redisCouponStockDecrementShouldNotAllowNegativeTest() {
//        // given
//        final Long couponId = 1L;
//        final long userId = 1L;
//        couponStockRedisManager.registerCoupon(couponId, "coupon-name", 0L);
//
//        // when
//        CouponIssuanceStatus response = couponStockRedisManager.decrementStock(couponId, userId);
//
//        // then
//        assertThat(response).isNotNull();
//        assertThat(response).isEqualTo(CouponIssuanceStatus.OUT_OF_STOCK);
//        assertThat(redisTemplate.opsForHash().get(BaseRedisManager.getCouponKey(couponId), CouponHashName.STOCK.name())).isEqualTo("-1");
//    }
//
//    @DisplayName("레디스에 쿠폰 정보가 없으면 쿠폰을 발급하지 않는다.")
//    @Test
//    void shouldSkipWhenCouponInfoNotExists() {
//        // given
//        final Long couponId = 1L;
//        final long userId = 1L;
//
//        // when
//        CouponIssuanceStatus response = couponStockRedisManager.decrementStock(couponId, userId);
//
//        // then
//        assertThat(response).isNotNull();
//        assertThat(response).isEqualTo(CouponIssuanceStatus.OUT_OF_STOCK);
//        assertThat(redisTemplate.opsForHash().get(BaseRedisManager.getCouponKey(couponId), CouponHashName.STOCK.name())).isEqualTo("-1");
//    }
//
//    @DisplayName("사용자가 wait-queue에 있다면 쿠폰을 발급하지 않는다.")
//    @Test
//    void sholudNotIssueWhenUserInWaitQueue() {
//        // given
//        final Long couponId = 1L;
//        final long userId = 1L;
//
//        Mockito.doNothing().when(moveFromWaitToEnterQueueScheduler).processScheduledWaitQueueTasks();
//
//        // when
//        couponQueueRedisManager.addCouponWaitQueue(couponId, userId);
//        CouponIssuanceStatus response = couponStockRedisManager.decrementStock(couponId, userId);
//
//        // then
//        assertThat(response).isNotNull();
//        assertThat(response).isEqualTo(CouponIssuanceStatus.ALREADY_IN_WAIT_QUEUE);
//        assertThat(redisTemplate.opsForHash().get(BaseRedisManager.getCouponKey(couponId), CouponHashName.STOCK.name())).isEqualTo("-1");
//    }
//}