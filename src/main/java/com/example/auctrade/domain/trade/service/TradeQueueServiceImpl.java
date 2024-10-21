package com.example.auctrade.domain.trade.service;

import java.util.HashSet;
import java.util.Set;

import org.redisson.api.RQueue;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TradeQueueServiceImpl implements TradeQueueService {

	private final RedissonClient redissonClient;

	private static final String PURCHASE_QUEUE_KEY = "purchase_queue";

	@Override
	public void addToQueue(Long userId) {
		RQueue<String> queue = redissonClient.getQueue(PURCHASE_QUEUE_KEY);
		queue.add(userId.toString());
		System.out.println("Added to queue: User ID = " + userId);
	}

	@Override
	public Set<Long> getQueue() {
		RQueue<String> queue = redissonClient.getQueue(PURCHASE_QUEUE_KEY);
		Set<Long> userIds = new HashSet<>();
		for (String id : queue.readAll()) {
			userIds.add(Long.parseLong(id));
		}
		return userIds;
	}

	@Override
	public void removeFromQueue(Long userKey) {
		RQueue<String> queue = redissonClient.getQueue(PURCHASE_QUEUE_KEY);
		queue.remove(userKey.toString());
	}
}
