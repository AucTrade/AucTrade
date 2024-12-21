package com.example.auctrade.domain.purchase.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class PurchaseDTO {

	@Getter
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class Purchase {
		private Integer quantity; // 구매 요청 수량
	}

	@Getter
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class Create {
		private Long limitId; // Limit 게시글 ID
		private Long buyerId; // 구매자 ID
		private Long sellerId; // 판매자 ID
		private Integer quantity; // 구매 수량
		private Boolean isAuction; // 경매 여부
	}

	@Getter
	@Builder
	@AllArgsConstructor
	public static class Get {
		private Long id;
		private Long limitId;
		private Long buyerId;
		private Long sellerId;
		private Integer quantity;
		private Long totalPrice;
		private Boolean isAuction;
		private LocalDateTime createdDate;
	}
}
