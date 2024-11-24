package com.example.auctrade.domain.trade.service;

import com.example.auctrade.domain.trade.dto.TradeDTO;

public interface TradeHandlerService {
	// 한정 판매 트랜잭션 처리 메서드
	TradeDTO.Get handleLimitTrade(TradeDTO.Create purchaseDTO);

	// 경매 트랜잭션 처리 메서드
	TradeDTO.Get handleAuctionTrade(TradeDTO.Create purchaseDTO);

	// Limit과 Auction 구분 후 각각의 메서드로 넘기는 공통 핸들링 메서드
	TradeDTO.Get handleTrade(TradeDTO.Create purchaseDTO);
}
