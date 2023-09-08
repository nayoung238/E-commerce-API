package com.nayoung.itemservice.domain.item;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ItemRedisRepository {

    private final RedisTemplate<String, String> redisTemplate;
    private final String PREFIX = "item:";
    private final String SUFFIX_QUANTITY = ":count";

    public void initializeItemStock(Long itemId, Long stock) {
        redisTemplate
                .opsForValue()
                .set(PREFIX + itemId + SUFFIX_QUANTITY, stock.toString());
    }

    public Long decrementItemStock(Long itemId, Long quantity) {
        return redisTemplate.opsForValue()
                .decrement(PREFIX + itemId + SUFFIX_QUANTITY, quantity);
    }

    public Long incrementItemStock(Long itemId, Long quantity) {
        return redisTemplate.opsForValue()
                .increment(PREFIX + itemId + SUFFIX_QUANTITY, quantity);
    }
}
