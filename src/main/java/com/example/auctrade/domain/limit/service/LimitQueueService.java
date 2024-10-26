package com.example.auctrade.domain.limit.service;

import com.example.auctrade.domain.limit.dto.LimitDTO;
import com.example.auctrade.domain.trade.dto.TradeDTO;

public interface LimitQueueService {
	// 사용자 대기열에 추가
	void addToQueue(Long userId);

	// 대기열에서 Limit 거래 요청 처리
	boolean processLimitPurchase(LimitDTO.Purchase purchaseDto, Long limitId, String buyer);
}
