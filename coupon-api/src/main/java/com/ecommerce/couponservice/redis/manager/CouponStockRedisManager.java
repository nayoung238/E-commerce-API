package com.ecommerce.couponservice.redis.manager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class CouponStockRedisManager extends BaseRedisManager {

    private final RedisTemplate<String, String> redisTemplate;
    private ObjectMapper objectMapper = new ObjectMapper();

    public void registerCoupon(Long couponId, String name, Long quantity) {
        String couponKey = getCouponKey(couponId);
        redisTemplate.execute(new SessionCallback<List<Object>>() {
            @Override
            public List<Object> execute(@NotNull RedisOperations operations) throws DataAccessException {
                operations.multi();

                @SuppressWarnings("unchecked")
                HashOperations<String, String, String> hashOps = operations.opsForHash();
                hashOps.put(couponKey, CouponHashName.NAME.name(), name);
                hashOps.put(couponKey, CouponHashName.STOCK.name(), quantity.toString());

                return operations.exec();
            }
        });
    }

    public Optional<Long> getStock(Long couponId) {
        Object value = redisTemplate.opsForHash().get(getCouponKey(couponId), CouponHashName.STOCK.name());
        return Optional.ofNullable(value)
                .map(v -> Long.parseLong(v.toString()));
    }

    public String getCouponName(Long couponId) {
        Object value = redisTemplate.opsForHash().get(getCouponKey(couponId), CouponHashName.NAME.name());
        return value != null ? value.toString() : null;
    }

    public CouponIssuanceStatus issueCouponUsingSessionCallback(Long couponId, Long userId) {
        return redisTemplate.execute(new SessionCallback<CouponIssuanceStatus>() {
            @Override
            public <K, V> CouponIssuanceStatus execute(RedisOperations<K, V> operations) throws DataAccessException {
                operations.multi();

                @SuppressWarnings("unchecked")
                RedisOperations<String, String> pipelinedOps = (RedisOperations<String, String>) operations;

                String couponKey = getCouponKey(couponId);
                String waitQueueKey = getWaitQueueKey(couponId);
                String enterQueueKey = getEnterQueueKey(couponId);

                pipelinedOps.opsForZSet().score(waitQueueKey, userId.toString());
                pipelinedOps.opsForHash().increment(couponKey, CouponHashName.STOCK.name(), -1);
                pipelinedOps.opsForZSet().remove(enterQueueKey, userId.toString());

                List<Object> results = operations.exec();
                if (results == null) {
                    return CouponIssuanceStatus.TRANSACTION_FAILED;
                }

                Double wasInWaitQueue = (Double) results.get(0);
                Long newStock = (Long) results.get(1);
                Long removedFromEnterQueue = (Long) results.get(2);

                if (wasInWaitQueue != null) {
                    return CouponIssuanceStatus.ALREADY_IN_WAIT_QUEUE;
                }
                if (newStock < 0) {
                    return CouponIssuanceStatus.OUT_OF_STOCK;
                }
                if (removedFromEnterQueue == 1) {
                    return CouponIssuanceStatus.SUCCESS;
                }
                return CouponIssuanceStatus.UNKNOWN_ERROR;
            }
        });
    }

    public Map<String, Object> issueCouponUsingLuaScript(Long couponId, Long userId) {
        String couponHashKey = getCouponKey(couponId);
        String waitQueueKey = getWaitQueueKey(couponId);
        String enterQueueKey = getEnterQueueKey(couponId);

        String script = "local is_contains_wait_queue = redis.call('ZSCORE', KEYS[4], KEYS[3]) " +
                        "if is_contains_wait_queue then " +
                            "redis.call('ZREM', KEYS[5], KEYS[3])" +
                            "return cjson.encode({status = 'ERROR', reason = 'Invalid queue state: User is in the wait queue.'}) " +
                        "end " +

                        "redis.call('ZREM', KEYS[5], KEYS[3]) " +
                        "local new_stock = redis.call('HINCRBY', KEYS[2], 'STOCK', -1) " +
                        "if new_stock >= 0 then " +
                            "return cjson.encode({status = 'SUCCESS', couponId = KEYS[1], newStock = new_stock}) " +
                        "else " +
                            "new_stock = redis.call('HINCRBY', KEYS[2], 'STOCK', 1) " +
                            "return cjson.encode({status = 'FAILED', reason = 'Out of stock: Insufficient coupon inventory'})" +
                        "end";

        RedisScript<String> redisScript = RedisScript.of(script, String.class);
        List<String> keys = List.of(couponId.toString(), couponHashKey, userId.toString(), waitQueueKey, enterQueueKey);
        String result = redisTemplate.execute(redisScript, keys);
        return convertMap(result);
    }

    public Map<String, Object> issueCouponAndPublishEvent(Long couponId, Long userId) {
        String couponHashKey = getCouponKey(couponId);
        String waitQueueKey = getWaitQueueKey(couponId);
        String enterQueueKey = getEnterQueueKey(couponId);
        String streamsKey = "coupon_stock_streams_key";

        String script = "local is_contains_wait_queue = redis.call('ZSCORE', KEYS[4], KEYS[3]) " +
                        "if is_contains_wait_queue then " +
                            "redis.call('ZREM', KEYS[5], KEYS[3])" +
                            "return cjson.encode({status = 'ERROR', reason = 'Invalid queue state: User is in the wait queue.'}) " +
                        "end " +

                        "redis.call('ZREM', KEYS[5], KEYS[3]) " +
                        "local new_stock = redis.call('HINCRBY', KEYS[2], 'STOCK', -1) " +
                        "if new_stock >= 0 then " +
                            "local stream_key = KEYS[6] " +
                            "redis.call('XADD', stream_key, '*', 'couponId', KEYS[1], 'newStock', new_stock) " +
                            "return cjson.encode({status = 'SUCCESS', couponId = KEYS[1], newStock = new_stock}) " +
                        "else " +
                            "new_stock = redis.call('HINCRBY', KEYS[2], 'STOCK', 1) " +
                            "return cjson.encode({status = 'FAILED', reason = 'Out of stock: Insufficient coupon inventory'}) " +
                        "end";

        RedisScript<String> redisScript = RedisScript.of(script, String.class);
        List<String> keys = List.of(couponId.toString(), couponHashKey, userId.toString(), waitQueueKey, enterQueueKey, streamsKey);
        String result = redisTemplate.execute(redisScript, keys);
        return convertMap(result);
    }

    Map<String, Object> convertMap(String resultJson) {
        if(resultJson == null || resultJson.isEmpty()) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(resultJson, new TypeReference<Map<String, Object>>() {});
        } catch(JsonProcessingException e) {
            log.error("Error processing JSON: {}", resultJson, e);
            return Collections.emptyMap();
        }
    }

    public void revertDecrementOperation(Long couponId) {
        redisTemplate.execute(new SessionCallback<Void>() {
            @Override
            public <K, V> Void execute(RedisOperations<K, V> operations) throws DataAccessException {
                RedisOperations<String, String> pipelinedOps = (RedisOperations<String, String>) operations;
                pipelinedOps.multi();

                pipelinedOps.opsForHash().increment(getCouponKey(couponId), CouponHashName.STOCK.name(), 1);

                pipelinedOps.exec();
                return null;
            }
        });
    }

    public Set<String> getCouponStockHashKeys() {
        return redisTemplate.keys(COUPON_KEY + "*");
    }
}
