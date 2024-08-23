package com.example.auctrade.domain.limit.service;

import java.util.Set;

public interface PurchaseQueueService {
	void addToQueue(String userId);
	Set<String> getQueue();
	void removeFromQueue(String userKey);
}
