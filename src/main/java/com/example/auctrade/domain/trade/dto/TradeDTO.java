package com.example.auctrade.domain.trade.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

public class TradeDTO {

	@Getter
	@Builder
	@AllArgsConstructor
	public static class Create {
		private int quantity; // 거래 수량
		private Long buyer; // 구매 회원 ID
		private Long postId; // 게시글 ID (Auction 또는 Limit의 ID)
		private Boolean isAuction; // 게시글 타입 (Auction이면 true, Limit이면 false)
	}

	@Getter
	@Builder
	public static class Get {
		private Long id; // 거래 ID
		private Long price; // 거래가
		private int quantity; // 거래 수량
		private LocalDateTime tradeDate; // 거래일
		private Boolean isFinished; // 거래 완료 여부
		private Long buyer; // 구매 회원 ID
		private Long postId; // 게시글 ID (Auction 또는 Limit의 ID)
		private Boolean isAuction; // 게시글 타입 (Auction이면 true, Limit이면 false)
	}
}
