package com.example.auctrade.domain.limit.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

public class PurchaseDTO {

	@Getter
	@Builder
	@AllArgsConstructor
	public static class Create {
		private int quantity;
		private Long userId;
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
}
