package com.example.auctrade.domain.limit.service;

import java.util.Set;
public interface PurchaseQueueService {
	void addToQueue(Long userId);
	Set<Long> getQueue();
	void removeFromQueue(Long userKey);
}

