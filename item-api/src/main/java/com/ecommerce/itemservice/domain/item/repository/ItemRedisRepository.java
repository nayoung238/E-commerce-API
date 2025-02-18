package com.ecommerce.itemservice.domain.item.repository;

import com.ecommerce.itemservice.exception.ExceptionCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Objects;

@Repository
@RequiredArgsConstructor
public class ItemRedisRepository {

    private final RedisTemplate<String, String> redisTemplate;
    private final String ITEM_PREFIX = "item:";
    private final String SUFFIX_QUANTITY = ":count";

    public void initializeItemStock(Long itemId, Long stock) {
        redisTemplate
                .opsForValue()
                .set(getItemStockKey(itemId), stock.toString());
    }

    public Boolean isExistItem(Long itemId) {
        return redisTemplate.hasKey(getItemStockKey(itemId));
    }

    public Long decrementItemStock(Long itemId, Long quantity) {
        if(!isExistItem(itemId)) {
            throw new IllegalArgumentException(String.valueOf(ExceptionCode.NOT_FOUND_ITEM_IN_REDIS));
        }
        return redisTemplate
                .opsForValue()
                .decrement(getItemStockKey(itemId), quantity);
    }

    public Long incrementItemStock(Long itemId, Long quantity) {
        if(!isExistItem(itemId)) {
            throw new IllegalArgumentException(String.valueOf(ExceptionCode.NOT_FOUND_ITEM_IN_REDIS));
        }
        return redisTemplate
                .opsForValue()
                .increment(getItemStockKey(itemId), quantity);
    }

    public Long findItemStock(Long itemId) {
        return Long.valueOf(Objects.requireNonNull(
                redisTemplate
                        .opsForValue()
                        .get(getItemStockKey(itemId))));
    }

    public void deleteKey(Long itemId) {
        redisTemplate.
                delete(getItemStockKey(itemId));
    }

    private String getItemStockKey(Long itemId) {
        return ITEM_PREFIX + itemId + SUFFIX_QUANTITY;
    }
}
