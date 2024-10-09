package com.ecommerce.couponservice.domain.coupon.service

import com.ecommerce.couponservice.kafka.config.TopicConfig
import com.ecommerce.couponservice.kafka.dto.CouponIssuanceResultKafkaEvent
import com.ecommerce.couponservice.kafka.service.producer.KafkaProducerService
import com.ecommerce.couponservice.redis.manager.CouponIssuanceStatus
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
                    val status = couponStockRedisManager.decrementStock(couponId, accountId)
                    when (status) {
                        CouponIssuanceStatus.SUCCESS -> {
                            log.info("쿠폰 발급 성공: couponId=$couponId, accountId=$accountId")
                            sendKafkaMessage(couponId, accountId)
                        }
                        else -> {
                            couponStockRedisManager.revertDecrementOperation(couponId)
                            log.info("쿠폰 발급 실패 및 수량 복구 완료: couponId=$couponId")
                        }
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