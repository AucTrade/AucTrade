package com.example.auctrade.domain.limit.service;

import java.util.Set;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PurchaseQueueServiceImpl implements PurchaseQueueService {

	private final RedisTemplate<String, String> customRedisTemplate;

	private static final String PURCHASE_QUEUE_KEY = "purchase_queue";

	@Override
	public void addToQueue(String userId) {
		double currentTime = (double) System.currentTimeMillis();
		customRedisTemplate.opsForZSet().add(PURCHASE_QUEUE_KEY, userId, currentTime);
		System.out.println("대기열에 추가됨: 사용자 ID = " + userId + ", 시간 = " + currentTime);
	}

	@Override
	public Set<String> getQueue() {
		return customRedisTemplate.opsForZSet().range(PURCHASE_QUEUE_KEY, 0, -1);
	}

	@Override
	public void removeFromQueue(String userKey) {
		customRedisTemplate.opsForZSet().remove(PURCHASE_QUEUE_KEY, userKey);
	}
}
