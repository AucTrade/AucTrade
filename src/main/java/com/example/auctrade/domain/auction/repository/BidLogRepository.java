package com.example.auctrade.domain.auction.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
@RequiredArgsConstructor
public class BidLogRepository {

    private final RedisTemplate<String, Integer> redisTemplate;

    // key: 경매 id, String hashKey: 회원 식별자, Integer hashValue: 입찰 가격
    public void saveHash(String key, Map<String, Integer> hash) {
        redisTemplate.opsForHash().putAll(key, hash);
    }

    public Map<Object, Object> getHash(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    public Integer getHashValue(String key, String hashKey) {
        return (Integer) redisTemplate.opsForHash().get(key, hashKey);
    }

    public void deleteHash(String key) {
        redisTemplate.delete(key);
    }
}
