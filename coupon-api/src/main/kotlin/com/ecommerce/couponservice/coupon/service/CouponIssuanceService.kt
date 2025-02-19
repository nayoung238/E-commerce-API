package com.ecommerce.couponservice.coupon.service

import com.ecommerce.couponservice.kafka.config.TopicConfig
import com.ecommerce.couponservice.kafka.dto.CouponIssuanceResultKafkaEvent
import com.ecommerce.couponservice.kafka.service.producer.KafkaProducerService
import com.ecommerce.couponservice.redis.manager.CouponStockRedisManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class CouponIssuanceService(
    private val couponStockRedisManager: CouponStockRedisManager,
    private val kafkaProducerService: KafkaProducerService
) {
    private val log = LoggerFactory.getLogger(CouponIssuanceService::class.java)

    suspend fun issueCouponInBatchAsync(couponId: Long, accountIds: List<Long>) {
        coroutineScope {
            accountIds.map { accountId ->
                launch(Dispatchers.IO) {
                    try {
                        val result: Map<String, Any> = couponStockRedisManager.issueCouponAndPublishEvent(couponId, accountId)
                        when (result["status"].toString()) {
                            "SUCCESS" -> {
                                val newStock = (result["newStock"] as Number).toLong()
                                sendKafkaMessage(couponId, accountId)
                                log.info("Coupon issued successfully: Coupon Id = $couponId, New Stock = $newStock, Account Id = $accountId")
                                // TODO: 쿠폰 발급 성공 알림
                            }
                            "FAILED" -> {
                                val reason = result["reason"] as String
                                log.warn("Coupon issuance failed: Coupon Id = $couponId, Reason = $reason")
                                // TODO: 쿠폰 발급 실패 알림
                            }
                            "ERROR" -> {
                                val reason = result["reason"] as String
                                log.error("Error during coupon issuance: Coupon Id = $couponId, Account Id = $accountId, Reason = $reason")
                            }
                        }
                    } catch (e: Exception) {
                        log.error("Unexpected error during coupon issuance: Coupon Id = $couponId, Account Id = $accountId, error = ${e.message}")
                    }
                }
            }
        }
    }

    private suspend fun sendKafkaMessage(couponId: Long, accountId: Long) {
        val couponName = couponStockRedisManager.getCouponName(couponId) ?: "Unnamed Coupon"
        val event = CouponIssuanceResultKafkaEvent.of(couponId, accountId, couponName);
        kafkaProducerService.send(TopicConfig.COUPON_ISSUANCE_RESULT_TOPIC, null, event);
    }
}