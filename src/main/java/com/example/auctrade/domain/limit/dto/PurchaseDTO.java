package com.example.auctrade.domain.limit.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

public class PurchaseDTO {

	@Getter
	@Builder
	@AllArgsConstructor
	public static class Create {
		private int quantity;
		private Long limitId;
	}

	@Getter
	@Builder
	public static class Get {
		private Long id;
		private Long price;
		private int quantity;
		private LocalDateTime date;
		private Boolean isFinished;
		private String buyerEmail;
		private String limitTitle;
	}

	@Getter
	@Builder
	@AllArgsConstructor
	public static class GetPage {
		private final List<PurchaseDTO.Get> purchases;
		private final Long maxPage;
	}
}
