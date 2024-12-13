package com.example.auctrade.domain.trade.mapper;

import com.example.auctrade.domain.trade.dto.TradeDTO;
import com.example.auctrade.domain.trade.entity.Trade;

import java.time.LocalDateTime;

public class TradeMapper {

	private TradeMapper() {}

	public static TradeDTO.Get toGetDto(Trade trade) {
		return (trade == null) ? null : TradeDTO.Get.builder()
			.id(trade.getId())
			.price(trade.getPrice())
			.quantity(trade.getQuantity())
			.tradeDate(trade.getTradeDate())
			.isFinished(trade.getIsFinished())
			.buyer(trade.getBuyer())
			.postId(trade.getPostId())
			.isAuction(trade.getIsAuction())
			.build();
	}

	public static Trade toEntity(TradeDTO.Create tradeDTO, long calculatedPrice) {
		return (tradeDTO == null) ? null : Trade.builder()
			.postId(tradeDTO.getPostId())
			.buyer(tradeDTO.getBuyerId().toString()) // 구매자 ID를 문자열로 변환
			.quantity(tradeDTO.getQuantity())
			.price(calculatedPrice)  // 계산된 가격
			.isAuction(tradeDTO.getIsAuction())
			.tradeDate(LocalDateTime.now()) // 현재 시간
			.isFinished(false)  // 거래 완료는 초기에는 false
			.build();
	}
}
