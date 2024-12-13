package com.example.auctrade.domain.trade.service;

import java.util.List;

import com.example.auctrade.domain.limit.dto.LimitDTO;
import com.example.auctrade.domain.trade.dto.TradeDTO;

public interface TradeService {
	// 한정 판매 거래 처리 메서드
	TradeDTO.Get processLimitTrade(TradeDTO.Create tradeDTO);

	// 경매 거래 처리 메서드
//	boolean processAuctionTrade(TradeDTO.Create tradeDTO);

	// 특정 사용자의 거래 내역 조회 메서드
	List<TradeDTO.Get> findTradesByUserId(Long userId);
}
