package com.example.auctrade.domain.limit.service;

import java.util.Set;

import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PurchaseQueueServiceImpl implements PurchaseQueueService {

	private final RedissonClient redissonClient;

	private static final String PURCHASE_QUEUE_KEY = "purchase_queue";

	@Override
	public void addToQueue(String userId) {
		double currentTime = System.currentTimeMillis();
		RScoredSortedSet<String> purchaseQueue = redissonClient.getScoredSortedSet(PURCHASE_QUEUE_KEY);
		purchaseQueue.add(currentTime, userId);
		System.out.println("Added to queue: User ID = " + userId + ", Time = " + currentTime);
	}

	@Override
	public Set<String> getQueue() {
		RScoredSortedSet<String> purchaseQueue = redissonClient.getScoredSortedSet(PURCHASE_QUEUE_KEY);
		return (Set<String>) purchaseQueue.readAll();
	}

	@Override
	public void removeFromQueue(String userKey) {
		RScoredSortedSet<String> purchaseQueue = redissonClient.getScoredSortedSet(PURCHASE_QUEUE_KEY);
		purchaseQueue.remove(userKey);
	}
}
