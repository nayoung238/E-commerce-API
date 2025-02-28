package com.ecommerce.couponservice.coupon.service

import com.ecommerce.couponservice.couponlog.service.CouponLogService
import com.ecommerce.couponservice.redis.manager.CouponStockRedisManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class CouponIssuanceService(
    private val couponStockRedisManager: CouponStockRedisManager,
    private val couponLogService: CouponLogService
) {
    private val log = LoggerFactory.getLogger(CouponIssuanceService::class.java)

    suspend fun issueCouponInBatchAsync(couponId: Long, userIds: List<Long>) {
        coroutineScope {
            userIds.map { userId ->
                launch(Dispatchers.IO) {
                    try {
                        val result: Map<String, Any> = couponStockRedisManager.issueCouponAndPublishEvent(couponId, userId)
                        when (result["status"].toString()) {
                            "SUCCESS" -> {
                                val newStock = (result["newStock"] as Number).toLong()
                                saveCouponLog(couponId, userId)
                                log.info("Coupon issued successfully: Coupon Id = $couponId, New Stock = $newStock, User Id = $userId")
                                // TODO: 쿠폰 발급 성공 알림
                            }
                            "FAILED" -> {
                                val reason = result["reason"] as String
                                log.warn("Coupon issuance failed: Coupon Id = $couponId, Reason = $reason")
                                // TODO: 쿠폰 발급 실패 알림
                            }
                            "ERROR" -> {
                                val reason = result["reason"] as String
                                log.error("Error during coupon issuance: Coupon Id = $couponId, User Id = $userId, Reason = $reason")
                            }
                        }
                    } catch (e: Exception) {
                        log.error("Unexpected error during coupon issuance: Coupon Id = $couponId, User Id = $userId, error = ${e.message}")
                    }
                }
            }
        }
    }

    private suspend fun saveCouponLog(couponId: Long, userId: Long) {
        withContext(Dispatchers.IO) {
            couponLogService.saveCouponLog(couponId, userId)
        }
    }
}