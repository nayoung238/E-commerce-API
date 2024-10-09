package com.ecommerce.couponservice.redis.scheduler

import com.ecommerce.couponservice.domain.coupon.service.CouponIssuanceService
import com.ecommerce.couponservice.redis.manager.CouponQueueRedisManager
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class CouponIssuanceScheduler(
    private val couponQueueRedisManager: CouponQueueRedisManager,
    private val couponIssuanceService: CouponIssuanceService
) : BaseCouponScheduler() {

    @Scheduled(fixedDelay = 5_000, initialDelay = 13_000)
    suspend fun processScheduleEnterQueueTasks() {
        val enterKeys = couponQueueRedisManager.enterQueueKeys
        if (enterKeys.isEmpty()) {
            log.debug("No enter queues found to process.")
            return
        }

        for (enterKey in enterKeys) {
            processEnterQueue(enterKey)
        }
    }

    private suspend fun processEnterQueue(enterKey: String) {
        try {
            val couponId = extractCouponId(CouponQueueRedisManager.ENTER_KEY_PREFIX, enterKey)
            if (couponId == null) {
                log.warn("Invalid enter queue key format: {}", enterKey)
                return
            }

            val accountIds = getAccountIdsFromEnterQueue(couponId)
            if (accountIds.isEmpty()) {
                log.warn("No valid accountIds found for couponId: {}", couponId)
                return
            }
            couponIssuanceService.issueCouponInBatchAsync(couponId, accountIds)
        } catch (e: NumberFormatException) {
            log.error("Failed to parse enter queue key: {}", enterKey, e)
        } catch (e: Exception) {
            log.error("Failed to process enter queue key: {}", enterKey, e)
        }
    }

    private fun getAccountIdsFromEnterQueue(couponId: Long): List<Long> {
        return couponQueueRedisManager.getEnterQueueValueAndScore(couponId)
            .mapNotNull { it.value }
            .mapNotNull { it.toLongOrNull() }
    }

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(CouponIssuanceScheduler::class.java)
    }
}