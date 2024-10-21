package com.example.auctrade.domain.trade.mapper;

import java.time.LocalDateTime;

import com.example.auctrade.domain.trade.dto.TradeDTO;
import com.example.auctrade.domain.trade.entity.Trade;

public class TradeMapper {

	private TradeMapper() {}

	// Entity -> DTO (Get)
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

	// DTO -> Entity (Create)
	public static Trade toEntity(TradeDTO.Create tradeDTO, long calculatedPrice) {
		return (tradeDTO == null) ? null : Trade.builder()
			.postId(tradeDTO.getPostId())
			.buyer(tradeDTO.getBuyer())
			.quantity(tradeDTO.getQuantity())
			.price(calculatedPrice)  // 계산된 가격을 사용
			.isAuction(tradeDTO.getIsAuction())
			.tradeDate(LocalDateTime.now()) // 현재 시간을 설정
			.isFinished(true)  // 거래 완료로 설정
			.build();
	}
}
