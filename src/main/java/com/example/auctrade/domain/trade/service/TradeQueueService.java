package com.example.auctrade.domain.trade.service;

import java.util.Set;
public interface TradeQueueService {
	void addToQueue(Long userId);
	Set<Long> getQueue();
	void removeFromQueue(Long userKey);
}

