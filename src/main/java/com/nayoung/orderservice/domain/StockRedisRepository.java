package com.nayoung.orderservice.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Objects;

@Repository
@RequiredArgsConstructor
public class StockRedisRepository {

    private final RedisTemplate<String, String> redisTemplate;
    private final String PREFIX = "item:";
    private final String SUFFIX_STOCK = ":count";

    public Long getItemStock(Long itemId) {
        return Long.parseLong(
                Objects.requireNonNull(
                        redisTemplate
                                .opsForValue()
                                .get(PREFIX + itemId + SUFFIX_STOCK)));
    }

    /**
     * Test에서만 사용
     * 실제 재고 초기화는 Item-Service에서 설정
     */
    public void initialStockQuantity(Long itemId, Long stock) {
        redisTemplate.opsForValue()
                .set(PREFIX + itemId + SUFFIX_STOCK, stock.toString());
    }
}
