package com.example.auctrade.domain.trade.service;

import java.util.List;

import com.example.auctrade.domain.trade.dto.TradeDTO;

public interface TradeService {
	TradeDTO.Get processTrade(TradeDTO.Create tradeDTO) ;
	List<TradeDTO.Get> findTradesByUserId(Long userId);
}
